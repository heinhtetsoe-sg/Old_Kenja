<?php

require_once('for_php7.php');

class knja320aForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja320aForm1", "POST", "knja320aindex.php", "", "knja320aForm1");

        //年度・学期
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　　".CTRL_SEMESTERNAME;

        //ラジオボタン
        $opt = array(1, 2);
        $model->field["CHUGAKU_KOUKOU"] = ($model->field["CHUGAKU_KOUKOU"] == "") ? "2" : $model->field["CHUGAKU_KOUKOU"];
        $extra = array("id=\"CHUGAKU_KOUKOU1\" onClick=\"change_chugaku_koukou('renrakumou_j.xls')\"", "id=\"CHUGAKU_KOUKOU2\" onClick=\"change_chugaku_koukou('renrakumou_h.xls')\"");
        $radioArray = knjCreateRadio($objForm, "CHUGAKU_KOUKOU", $model->field["CHUGAKU_KOUKOU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //テンプレートのダウンロード
        $arg["REQUESTROOT"] = REQUESTROOT;
        $arg["RENRAKU_XLS"] = ($model->field["CHUGAKU_KOUKOU"] == "1") ? "renrakumou_j.xls" : "renrakumou_h.xls";

        //クラス選択コンボ
        $opt_class = array();
        $db = Query::dbCheckOut();
        $query = common::getHrClassAuth(CTRL_YEAR,CTRL_SEMESTER,AUTHORITY,STAFFCD);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_class[]= array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "options"    => $opt_class));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //電話番号2(HR担任)
        $objForm->ae( array("type"        => "text",
                            "name"        => "STAFF_TELNO",
                            "size"        => 15,
                            "maxlength"   => 14,
                            "extrahtml"   => "",
                            "value"       => $model->field["STAFF_TELNO"]));

        $arg["data"]["STAFF_TELNO"] = $objForm->ge("STAFF_TELNO");

        //電話番号
        $opt = array(1, 2);
        $model->field["TEL_NO"] = ($model->field["TEL_NO"] == "") ? "1" : $model->field["TEL_NO"];
        $extra = array("id=\"TEL_NO1\"", "id=\"TEL_NO2\"");
        $radioArray = knjCreateRadio($objForm, "TEL_NO", $model->field["TEL_NO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //csvボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "ＣＳＶ出力",
                            "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );


        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja320aForm1.html", $arg);
    }
}
?>
