from __future__ import annotations

from pathlib import Path
import yaml
import re

_MESSAGES = yaml.safe_load((Path(__file__).parent / "templates" / "messages.yaml").read_text(encoding="utf-8"))


def render_from_body(body: dict) -> str:
    # Простая заглушка: если есть поле template, вернём сохранённый текст, иначе str
    template_key = body.get("template") if isinstance(body, dict) else None
    if template_key and template_key in _MESSAGES:
        template = _MESSAGES[template_key]
        params = body.get("params", {})
        result = render_template(template, params)
        print(f"🔧 Rendering template '{template_key}' with params {params} -> {result}")
        return result
    return str(body) if body else ""


def render_template(template: str, params: dict) -> str:
    """Подставляет параметры в шаблон сообщения"""
    result = template
    
    # Заменяем плейсхолдеры вида {{param_name}}
    for param_name, param_value in params.items():
        placeholder = f"{{{{{param_name}}}}}"
        # Убеждаемся, что значение корректно конвертируется в строку с UTF-8
        str_value = str(param_value) if param_value is not None else ""
        result = result.replace(placeholder, str_value)
    
    return result



