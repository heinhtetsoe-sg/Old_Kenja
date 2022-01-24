<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjb0031SubForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform1", "POST", "knjb0031index.php", "", "subform1");

        $arg["NAME_SHOW"] = substr($model->term, 0, 4)."年度  ".$model->control["学期名"][substr($model->term, 5)];

        $db = Query::dbCheckOut();

        //コースコンボで指定した学籍在籍データの件数
        $grade_course = VARS::get("grade_course");
        $regdCnt = $db->getOne(knjb0031Query::checkRegdDatSubForm1($model->term, $grade_course));

        //SQL文発行
        //組略称（学年・組）取得
        $query = knjb0031Query::selectQuerySubForm1($model->term, $grade_course, $regdCnt);
        $result = $db->query($query);
        $i = 0;
        $param = VARS::get("param");
        $counter = VARS::get("counter");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            if (strstr($param, $row["GRADE_CLASS"])) {
                $check = "checked";
            } else {
                $check = "";
            }
            //選択（チェック）
            $objForm->ae(array("type"        => "checkbox",
                               "name"          => "CHECK",
                               "value"          => $row["GRADE_CLASS"].",".$row["HR_NAMEABBV"],
                               "extrahtml"    => $check,
                               "multiple"    => "1" ));

            $row["CHECK"] = $objForm->ge("CHECK");

            $row["backcolor"] = (strstr($param, $row["GRADE_CLASS"])) ? "#ccffcc" : "#ffffff";  //#ccffff
//            $row["backcolor"] = "#ffffff";
            $arg["data"][] = $row;
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);


        //選択ボタンを作成する
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_sentaku",
                            "value"     => "選 択",
                            "extrahtml" => "onclick=\"return btn_submit('{$i}', '{$counter}')\"" ));

        $arg["btn_sentaku"] = $objForm->ge("btn_sentaku");

        //終了ボタンを作成する
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"return parent.closeit()\"" ));

        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd" ));

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb0031SubForm1.html", $arg);
    }
}
