from pydub import AudioSegment
from datetime import datetime

global split_duration_ms
split_duration_ms = 5000 

def split_audio_file(input_file, output_path, name):
    audio = AudioSegment.from_wav(input_file)
    duration_ms = len(audio)
    
    for i in range(0, duration_ms, split_duration_ms):
        start_ms = i
        end_ms = min(i + split_duration_ms, duration_ms)
        split_audio = audio[start_ms:end_ms]
        output_filename = f"{output_path}/{name}_{i//5000}.wav"
        split_audio.export(output_filename, format="wav")
