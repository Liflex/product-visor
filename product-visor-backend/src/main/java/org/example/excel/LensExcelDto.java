package org.example.excel;

import lombok.Data;

@Data
public class LensExcelDto {
    // Название товара (Product.name)
    private String productName;
    // Артикул товара (Product.article)
    private String article;
    // Штрих-код (Product.barcode)
    private String barcode;
    // Цена (Product.price)
    private String price;
    // Категория (Product.category.name)
    private String categoryName;
    // Количество на складе (Product.quantity)
    private String quantity;

    // Упаковка: ширина (PackageInfo.width)
    private String packageWidth;
    // Упаковка: высота (PackageInfo.height)
    private String packageHeight;
    // Упаковка: длина (PackageInfo.length)
    private String packageLength;
    // Упаковка: вес (PackageInfo.weight)
    private String packageWeight;
    // Упаковка: кол-во в упаковке (PackageInfo.quantityInPackage)
    private String packageQuantityInPack;

    // Атрибут: Цвет (attribute name = "color")
    private String attrColor;
    // Атрибут: Диоптрии (attribute name = "dioptries")
    private String attrDioptries;
    // Атрибут: Срок годности (множественный) (attribute name = "date_expired") — список дат через ;
    private String attrDateExpired;
    // Атрибут: Диаметр (attribute name = "diameter")
    private String attrDiameter;
    // Атрибут: Кривизна (attribute name = "curvature")
    private String attrCurvature;
} 