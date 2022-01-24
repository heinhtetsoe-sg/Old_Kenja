<?php

require_once('for_php7.php');

class knjc201Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("knjc201Form1", "POST", "knjc201index.php", "", "knjc201Form1");

        //年度
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        $db = Query::dbCheckOut();

        //クラス選択 1:年・組順,2:担任あいうえお順
        $model->field["OUTPUT"] = isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : 1;
        knjCreateHidden($objForm, "OUTPUT", $model->field["OUTPUT"]);

        //年・組順
        $cmp_grade   = "";
        $opt_hrname1 = array();
        $result      = $db->query(knjc201Query::getHrName($model, 1));
        $str1 = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($cmp_grade != $row["GRADE"] && $cmp_grade != "") {
                $str1 .= "</optgroup>";
            }
            if ($cmp_grade != $row["GRADE"]) {
                $str1 .= "<optgroup label=\"".$row["GRADE_NAME1"]."\">";
            }
            $str1 .= "<option value='".$row["GRADE"] .$row["HR_CLASS"]."'>" .$row["HR_NAME"] ." - " .$row["STAFFNAME"];
            $cmp_grade = $row["GRADE"];
        }
        if ($cmp_grade != "") {
            $str1 .= "</optgroup>";
        }
        //担任あいうえお順
        $cmp_kana1   = "";
        $opt_hrname2 = array();
        $result = $db->query(knjc201Query::getHrName($model, 2));
        $str2 = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($cmp_kana1 != $row["KANA1"] && $cmp_kana1 != "") {
                $str2 .= "</optgroup>";
            }
            if ($cmp_kana1 != $row["KANA1"]) {
                $str2 .= "<optgroup label=\"".$row["KANA1"]."\">";
            }
            $str2 .= "<option value='".$row["GRADE"] .$row["HR_CLASS"]."'>" .$row["STAFFNAME"] ." - " .$row["HR_NAME"];
            $cmp_kana1 = $row["KANA1"];
        }
        if ($cmp_kana1 != "") {
            $str2 .= "</optgroup>";
        }

        $arg["data"]["SORT_NAME"] = ($model->field["OUTPUT"] == 1) ? "クラス選択画面<br>(年・組順)" : "クラス選択画面<br>(担任あいうえお順)";
        $arg["data"]["HR_NAME"]   = ($model->field["OUTPUT"] == 1) ? $str1 : $str2;

        $result->free();
        Query::dbCheckIn($db);

        //クラス選択画面へ
        $sort_name = ($model->field["OUTPUT"] == 2) ? "クラス選択画面\n(年・組順)" : "クラス選択画面\n(担任あいうえお順)";
        $sort_width = ($model->field["OUTPUT"] == 2) ? "180" : "240";
        $extra = "style=\"width:{$sort_width}px\" onclick=\"return btn_submit('output');\"";
        $arg["button"]["btn_sort"] = knjCreateBtn($objForm, "btn_sort", $sort_name, $extra);

        //リンク先作成
        $jumping2 = REQUESTROOT."/C/KNJC201_2/knjc201_2index.php";

        //詳細入力画面へ
        $extra = "onclick=\"btn_select('".$jumping2."');\"";
        $arg["button"]["btn_disp1"] = knjCreateBtn($objForm, "btn_disp1", "詳細入力画面へ", $extra);

        //生徒検索
        $jump_search = REQUESTROOT."/C/KNJC200_SEARCH/knjc200_searchindex.php";
        $extra = "onclick=\"btn_seito('".$jump_search."');\"";
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "生徒検索", $extra);

        //ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //リンク先作成
        $jumping = REQUESTROOT."/C/KNJC201_ABSENCE_LIST/knjc201_absence_listindex.php";
        //リンクボタンを作成する
        $extra = "onclick=\"Page_jumper('" . $jumping . "');\"";
        $arg["button"]["btn_disp3"] = knjCreateBtn($objForm, "btn_disp3", "一覧確認", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc201Form1.html", $arg);
    }
}
