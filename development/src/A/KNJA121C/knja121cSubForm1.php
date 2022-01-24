<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja121cSubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;
        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knja121cindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //表示項目
        $field_name_array = array('HREPORTREMARK_DAT__TOTALSTUDYTIME',
                                  'HREPORTREMARK_DAT__COMMUNICATION');

        //通知表所見表示
        $query = knja121cQuery::getHreportRemarkDat($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //総合的な学習の時間
            $extra = "style=\"height:75px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"";
            $row["TOTALSTUDYTIME"] = knjCreateTextArea($objForm, "TOTALSTUDYTIME", 4, 43, "soft", $extra, $row["TOTALSTUDYTIME"]);

            //担任からの所見
            $extra = "style=\"height:75px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"";
            $row["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", 4, 43, "soft", $extra, $row["COMMUNICATION"]);

            $sub_arg = array();
            $sub_arg["datas"][] = array("DATA" => $row["SEMESTERNAME"]);
            foreach ($model->Properties as $key => $val) {
                if (in_array($key, $field_name_array) && strlen($val)) {
                    list($table_name, $filed_name) = preg_split("/__/", $key);
                    $sub_arg["datas"][] = array("DATA" => $row[$filed_name]);
                }
            }

            $arg["data_array"][] = $sub_arg;
        }
        $colspan_count = 0;
        //項目名表示
        foreach ($model->Properties as $key => $val) {
            if (in_array($key, $field_name_array) && strlen($val)) {
                list($table_name, $filed_name) = preg_split("/__/", $key);
                $arg["LABELS"][] = array("LABEL" => $val);
                $colspan_count++;
            }
        }

        $arg["colspan"] = $colspan_count;

        //終了ボタン
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja121cSubForm1.html", $arg);
    }
}
?>
