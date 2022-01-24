drop function TRANSLATE_H_HK

CREATE FUNCTION TRANSLATE_H_HK \
(   henkanStr varchar(240) \
) RETURNS varchar(240) \
 CONTAINS SQL \
 SPECIFIC TRANSLATE_H_HK \
 LANGUAGE SQL \ 
 NO EXTERNAL ACTION \
 DETERMINISTIC \
        RETURN (VALUES(translate(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(henkanStr, 'ぽ', 'ﾎﾟ'), 'ぼ', 'ﾎﾞ'), 'ぺ', 'ﾍﾟ'), 'べ', 'ﾍﾞ'), 'ぷ', 'ﾌﾟ'), 'ぶ', 'ﾌﾞ'), 'ぴ', 'ﾋﾟ'), 'び', 'ﾋﾞ'), 'ぱ', 'ﾊﾟ'), 'ば', 'ﾊﾞ'), 'ど', 'ﾄﾞ'), 'で', 'ﾃﾞ'), 'づ', 'ﾂﾞ'), 'ぢ', 'ﾁﾞ'), 'だ', 'ﾀﾞ'), 'ぞ', 'ｿﾞ'), 'ぜ', 'ｾﾞ'), 'ず', 'ｽﾞ'), 'じ', 'ｼﾞ'), 'ざ', 'ｻﾞ'), 'ご', 'ｺﾞ'), 'げ', 'ｹﾞ'), 'ぐ', 'ｸﾞ'), 'ぎ', 'ｷﾞ'), 'が', 'ｶﾞ'), \
                             'ｧｱｨｲｩｳｪｴｫｵｶｷｸｹｺｻｼｽｾｿﾀﾁｯﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓｬﾔｭﾕｮﾖﾗﾘﾙﾚﾛヮﾜｲｴｦﾝ', \
                             'ぁあぃいぅうぇえぉおかきくけこさしすせそたちっつてとなにぬねのはひふへほまみむめもゃやゅゆょよらりるれろゎわゐゑをん')))
