import os
import glob

def combine_text_files(directory, output_file, file_extension="*.*"):
    """
    Обходит каталог и объединяет текстовые файлы

    Args:
        directory (str): Путь к каталогу для обхода
        output_file (str): Имя выходного файла
        file_extension (str): Расширение файлов для обработки
    """

    # Получаем список всех текстовых файлов в каталоге и подкаталогах
    search_pattern = os.path.join(directory, "**", file_extension)
    text_files = glob.glob(search_pattern, recursive=True)

    if not text_files:
        print(f"Текстовые файлы с расширением {file_extension} не найдены в {directory}")
        return

    print(f"Найдено {len(text_files)} файлов для обработки")

    with open(output_file, 'w', encoding='utf-8') as outfile:
        for file_path in text_files:
            if "build/" in file_path: continue
            if "logs/" in file_path: continue

            try:
                with open(file_path, 'r', encoding='utf-8') as infile:
                    # Добавляем разделитель с именем файла
                    outfile.write(f"\n{'='*50}\n")
                    outfile.write(f"Файл: {file_path}\n")
                    outfile.write(f"{'='*50}\n\n")

                    # Копируем содержимое файла
                    content = infile.read()
                    outfile.write(content)

                    # Добавляем пустую строку между файлами
                    outfile.write("\n\n")

                print(f"Обработан: {file_path}")

            except Exception as e:
                print(f"Ошибка при обработке файла {file_path}: {e}")

    print(f"Все файлы объединены в: {output_file}")

# Использование
if __name__ == "__main__":
    directory_path = input("Введите путь к каталогу: ").strip()
    output_filename = input("Введите имя выходного файла (по умолчанию: combined_files.txt): ").strip()

    if not directory_path:
        directory_path = "./"
    if not output_filename:
        output_filename = "combined_files.txt"

    if os.path.exists(directory_path):
        combine_text_files(directory_path, output_filename)
    else:
        print("Указанный каталог не существует!")