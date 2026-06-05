package com.nkwabyte.medilert.data.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image as SkiaImage
import platform.darwin.NSObject
import platform.posix.memcpy
import platform.UIKit.*

// Retained while a picker is active to prevent ARC from deallocating the delegate.
private var activeDelegateHolder: Any? = null

actual class ImagePicker(
    internal var onImagePicked: (ByteArray) -> Unit
) {
    actual fun pickFromGallery() {
        presentPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)
    }

    actual fun pickFromCamera() {
        if (UIImagePickerController.isSourceTypeAvailable(
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            )
        ) {
            presentPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)
        }
    }

    private fun presentPicker(sourceType: UIImagePickerControllerSourceType) {
        val pickerVC = UIImagePickerController()
        pickerVC.sourceType = sourceType
        pickerVC.allowsEditing = true

        val delegate = PickerDelegate { bytes ->
            bytes?.let { onImagePicked(it) }
            activeDelegateHolder = null
        }
        pickerVC.delegate = delegate
        activeDelegateHolder = delegate

        @Suppress("DEPRECATION")
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootVC?.presentViewController(pickerVC, animated = true, completion = null)
    }
}

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): ImagePicker {
    val picker = remember { ImagePicker(onImagePicked) }
    picker.onImagePicked = onImagePicked
    return picker
}

actual fun ByteArray.decodeToImageBitmap(): ImageBitmap =
    SkiaImage.makeFromEncoded(this).toComposeImageBitmap()

@Suppress("CONFLICTING_OVERLOADS")
private class PickerDelegate(
    private val onResult: (ByteArray?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val key = if (picker.allowsEditing) UIImagePickerControllerEditedImage
                  else UIImagePickerControllerOriginalImage
        val image = (didFinishPickingMediaWithInfo[key] as? UIImage)
            ?: (didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage)

        picker.dismissViewControllerAnimated(true, completion = null)

        val bytes = image?.let { uiImage ->
            UIImageJPEGRepresentation(uiImage, 0.85)?.let { nsData ->
                ByteArray(nsData.length.toInt()).also { arr ->
                    arr.usePinned { pinned ->
                        memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
                    }
                }
            }
        }
        onResult(bytes)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onResult(null)
    }
}
