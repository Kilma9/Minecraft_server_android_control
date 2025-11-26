# Android Icon Generator
# This script converts your icon to all required Android sizes

from PIL import Image
import os

# Icon sizes for different densities
sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

# Load original icon
icon_path = r'C:\Users\Kuba\Desktop\ICON_android.jpg'
output_base = r'C:\Users\Kuba\Desktop\Kilma_Android_App\app\src\main\res'

try:
    img = Image.open(icon_path)
    print(f"Original icon size: {img.size}")
    
    # Convert to RGBA if needed
    if img.mode != 'RGBA':
        img = img.convert('RGBA')
    
    # Generate all sizes
    for folder, size in sizes.items():
        output_dir = os.path.join(output_base, folder)
        os.makedirs(output_dir, exist_ok=True)
        
        # Resize image
        resized = img.resize((size, size), Image.Resampling.LANCZOS)
        
        # Save as PNG
        output_file = os.path.join(output_dir, 'ic_launcher.png')
        resized.save(output_file, 'PNG')
        print(f"Created: {folder}/ic_launcher.png ({size}x{size})")
        
        # Also create round version
        output_file_round = os.path.join(output_dir, 'ic_launcher_round.png')
        resized.save(output_file_round, 'PNG')
        print(f"Created: {folder}/ic_launcher_round.png ({size}x{size})")
    
    print("\nIcon generation complete!")
    print("Now rebuild your app with: .\\gradlew assembleDebug")
    
except Exception as e:
    print(f"Error: {e}")
    print("\nPillow library not installed!")
    print("Install it with: pip install Pillow")
