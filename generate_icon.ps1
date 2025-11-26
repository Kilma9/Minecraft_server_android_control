# Android Icon Generator PowerShell Script with Inner Circle Cropping
Add-Type -AssemblyName System.Drawing

$iconPath = "C:\Users\Kuba\Desktop\ICON_android.jpg"
$outputBase = "C:\Users\Kuba\Desktop\Kilma_Android_App\app\src\main\res"

$sizes = @{
    'mipmap-mdpi' = 48
    'mipmap-hdpi' = 72
    'mipmap-xhdpi' = 96
    'mipmap-xxhdpi' = 144
    'mipmap-xxxhdpi' = 192
}

# Crop to inner dark circle only (exclude outer cyan ring)
# The inner circle is approximately 70% of the total image size
$innerCircleRatio = 0.70

try {
    $originalImage = [System.Drawing.Image]::FromFile($iconPath)
    Write-Host "Original icon size: $($originalImage.Width)x$($originalImage.Height)"
    
    foreach ($folder in $sizes.Keys) {
        $size = $sizes[$folder]
        
        $outputDir = Join-Path $outputBase $folder
        
        if (!(Test-Path $outputDir)) {
            New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
        }
        
        # Calculate the source rectangle for the inner circle crop
        $sourceSize = [Math]::Min($originalImage.Width, $originalImage.Height)
        $cropSize = [int]($sourceSize * $innerCircleRatio)
        $cropX = [int](($originalImage.Width - $cropSize) / 2)
        $cropY = [int](($originalImage.Height - $cropSize) / 2)
        $sourceRect = New-Object System.Drawing.Rectangle($cropX, $cropY, $cropSize, $cropSize)
        
        # Create square icon with circular clipping
        $resized = New-Object System.Drawing.Bitmap($size, $size)
        $graphics = [System.Drawing.Graphics]::FromImage($resized)
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        
        # Create circular clipping path
        $circlePath = New-Object System.Drawing.Drawing2D.GraphicsPath
        $circlePath.AddEllipse(0, 0, $size, $size)
        $graphics.SetClip($circlePath)
        
        # Fill with transparent background
        $graphics.Clear([System.Drawing.Color]::Transparent)
        
        # Draw cropped inner circle
        $destRect = New-Object System.Drawing.Rectangle(0, 0, $size, $size)
        $graphics.DrawImage($originalImage, $destRect, $sourceRect, [System.Drawing.GraphicsUnit]::Pixel)
        
        $outputFile = Join-Path $outputDir "ic_launcher.png"
        $resized.Save($outputFile, [System.Drawing.Imaging.ImageFormat]::Png)
        Write-Host "Created: $folder\ic_launcher.png (${size}x${size} inner circle only)"
        
        $circlePath.Dispose()
        
        # Create round icon (same as square)
        $roundBitmap = New-Object System.Drawing.Bitmap($size, $size)
        $roundGraphics = [System.Drawing.Graphics]::FromImage($roundBitmap)
        $roundGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $roundGraphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        
        # Create circular clipping path
        $path = New-Object System.Drawing.Drawing2D.GraphicsPath
        $path.AddEllipse(0, 0, $size, $size)
        $roundGraphics.SetClip($path)
        
        # Fill background (transparent)
        $roundGraphics.Clear([System.Drawing.Color]::Transparent)
        
        # Draw the cropped inner circle
        $roundGraphics.DrawImage($originalImage, $destRect, $sourceRect, [System.Drawing.GraphicsUnit]::Pixel)
        
        $outputFileRound = Join-Path $outputDir "ic_launcher_round.png"
        $roundBitmap.Save($outputFileRound, [System.Drawing.Imaging.ImageFormat]::Png)
        Write-Host "Created: $folder\ic_launcher_round.png (${size}x${size} inner circle only)"
        
        $path.Dispose()
        $roundGraphics.Dispose()
        $roundBitmap.Dispose()
        $graphics.Dispose()
        $resized.Dispose()
    }
    
    $originalImage.Dispose()
    Write-Host "`nIcon generation complete!"
    Write-Host "Now rebuild your app with: .\gradlew assembleDebug"
} catch {
    Write-Host "Error: $_"
}
