<?php

require_once('for_php7.php');

    //コンボ作成
    function knjCreateCombo(&$objForm, $name, $value, $options, $extra, $size, $multiple = "")
    {
        $setType = $multiple == "1" ? "select multiple" : "select";
        $objForm->ae( array("type"      => $setType,
                            "name"      => $name,
                            "size"      => $size,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "options"   => $options));
        return $objForm->ge($name);
    }

    //ラジオ作成
    function knjCreateRadio(&$objForm, $name, $value, $extra, $multi, $count)
    {
        $ret = array();

        for ($i = 1; $i <= $count; $i++) {
            if (is_array($extra)) $ext = $extra[$i-1];
            else $ext = $extra;
            
            $objForm->ae( array("type"      => "radio",
                                "name"      => $name,
                                "value"     => $value,
                                "extrahtml" => $ext,
                                "multiple"  => $multi));
            $ret[$name.$i] = $objForm->ge($name, $i);
        }

        return $ret;
    }

    //テキスト作成
    function knjCreatePassword(&$objForm, $data, $name, $size, $maxlen, $extra)
    {
        $objForm->ae( array("type"      => "text",
                            "name"      => $name,
                            "size"      => $size,
                            "maxlength" => $maxlen,
                            "value"     => $data,
                            "pass"      => "true",
                            "extrahtml" => $extra) );
        return $objForm->ge($name);
    }

    //テキスト作成
    function knjCreateTextBox(&$objForm, $data, $name, $size, $maxlen, $extra, $multiple = "")
    {
        $objForm->ae( array("type"      => "text",
                            "name"      => $name,
                            "size"      => $size,
                            "maxlength" => $maxlen,
                            "multiple"  => $multiple,
                            "value"     => $data,
                            "extrahtml" => $extra) );
        return $objForm->ge($name);
    }

    //テキストエリア作成
    function knjCreateTextArea(&$objForm, $name, $rows, $cols, $wrap, $extra, $value)
    {
        $objForm->ae( array("type"        => "textarea",
                            "name"        => $name,
                            "rows"        => $rows,
                            "cols"        => $cols,
                            "wrap"        => $wrap,
                            "extrahtml"   => $extra,
                            "value"       => $value));
        return $objForm->ge($name);
    }

    //チェックボックス作成
    function knjCreateCheckBox(&$objForm, $name, $value, $extra, $multi = "")
    {

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "multiple"  => $multi));

        return $objForm->ge($name);
    }

    //ボタン作成
    function knjCreateBtn(&$objForm, $name, $value, $extra, $type = "button")
    {
        $objForm->ae( array("type"      => $type,
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra));
        return $objForm->ge($name);
    }

    //Hidden作成ae
    function knjCreateHidden(&$objForm, $name, $value = "")
    {
        $objForm->ae( array("type"      => "hidden",
                            "name"      => $name,
                            "value"     => $value));

        return $objForm->ge($name);
    }

    //File作成
    function knjCreateFile(&$objForm, $name, $extra, $size)
    {
        $objForm->add_element(array("type"      => "file",
                                    "name"      => $name,
                                    "size"      => $size,
                                    "extrahtml" => $extra ));
        return $objForm->ge($name);
    }

?>