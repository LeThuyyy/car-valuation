/**
 * Car Valuation AI - Browser Camera & OCR Helper
 * Sử dụng navigator.mediaDevices.getUserMedia() để chụp ảnh từ camera và gửi tới Backend
 */

class CarCamera {
    constructor(videoElementId, canvasElementId) {
        this.video = document.getElementById(videoElementId);
        this.canvas = document.getElementById(canvasElementId);
        this.stream = null;
        this.currentFacingMode = 'environment'; // Mặc định dùng camera sau trên điện thoại
    }

    async startCamera() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            throw new Error("Trình duyệt của bạn không hỗ trợ Camera API. Vui lòng sử dụng tính năng tải ảnh từ máy.");
        }

        this.stopCamera();

        const constraints = {
            video: {
                facingMode: { ideal: this.currentFacingMode },
                width: { ideal: 1920 },
                height: { ideal: 1080 }
            },
            audio: false
        };

        try {
            this.stream = await navigator.mediaDevices.getUserMedia(constraints);
            this.video.srcObject = this.stream;
            await this.video.play();
            return true;
        } catch (err) {
            console.warn("Không mở được camera sau, thử mở camera mặc định:", err);
            try {
                this.stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
                this.video.srcObject = this.stream;
                await this.video.play();
                return true;
            } catch (fallbackErr) {
                console.error("Camera access denied or unavailable:", fallbackErr);
                throw new Error("Không thể truy cập camera. Vui lòng cấp quyền hoặc tải ảnh trực tiếp từ máy.");
            }
        }
    }

    stopCamera() {
        if (this.stream) {
            this.stream.getTracks().forEach(track => track.stop());
            this.stream = null;
        }
        if (this.video) {
            this.video.srcObject = null;
        }
    }

    switchCamera() {
        this.currentFacingMode = (this.currentFacingMode === 'environment') ? 'user' : 'environment';
        return this.startCamera();
    }

    captureBase64(quality = 0.85) {
        if (!this.video || !this.video.videoWidth) {
            throw new Error("Camera chưa sẵn sàng chụp");
        }

        this.canvas.width = this.video.videoWidth;
        this.canvas.height = this.video.videoHeight;
        const ctx = this.canvas.getContext('2d');
        ctx.drawImage(this.video, 0, 0, this.canvas.width, this.canvas.height);

        return this.canvas.toDataURL('image/jpeg', quality);
    }
}

// Utility: Đọc file từ input[type="file"] sang chuỗi base64
function readFileAsBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
        reader.readAsDataURL(file);
    });
}
