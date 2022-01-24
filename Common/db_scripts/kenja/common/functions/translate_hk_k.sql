drop function TRANSLATE_HK_K

CREATE FUNCTION TRANSLATE_HK_K \
(   henkanStr varchar(240) \
) RETURNS varchar(240) \
 CONTAINS SQL \
 SPECIFIC TRANSLATE_HK_K \
 LANGUAGE SQL \ 
 NO EXTERNAL ACTION \
 DETERMINISTIC \
        RETURN (VALUES(translate(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(henkanStr, 'ｶﾞ', 'ガ'), 'ｷﾞ', 'ギ'), 'ｸﾞ', 'グ'), 'ｹﾞ', 'ゲ'), 'ｺﾞ', 'ゴ'), 'ｻﾞ', 'ザ'), 'ｼﾞ', 'ジ'), 'ｽﾞ', 'ズ'), 'ｾﾞ', 'ゼ'), 'ｿﾞ', 'ゾ'), 'ﾀﾞ', 'ダ'), 'ﾁﾞ', 'ヂ'), 'ﾂﾞ', 'ヅ'), 'ﾃﾞ', 'デ'), 'ﾄﾞ', 'ド'), 'ﾊﾞ', 'バ'), 'ﾊﾟ', 'パ'), 'ﾋﾞ', 'ビ'), 'ﾋﾟ', 'ピ'), 'ﾌﾞ', 'ブ'), 'ﾌﾟ', 'プ'), 'ﾍﾞ', 'ベ'), 'ﾍﾟ', 'ペ'), 'ﾎﾞ', 'ボ'), 'ﾎﾟ', 'ポ'), 'ｳﾞ', 'ヴ'), \
                             'ァアィイゥウェエォオカキクケコサシスセソタチッツテトナニヌネノハヒフヘホマミムメモャヤュユョヨラリルレロヮワヲン', \
                             'ｧｱｨｲｩｳｪｴｫｵｶｷｸｹｺｻｼｽｾｿﾀﾁｯﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓｬﾔｭﾕｮﾖﾗﾘﾙﾚﾛヮﾜｦﾝ')))
