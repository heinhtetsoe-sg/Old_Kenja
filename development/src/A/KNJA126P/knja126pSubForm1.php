<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja126pSubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;
        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knja126pindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //表示項目
        $chk_gdat = $db->getOne(knja126pQuery::checkSchregRegdGdat($model));
        if ($chk_gdat) {
            $field_name_array = array('HREPORTREMARK_DAT__TOTALSTUDYTIME',
                                      'HREPORTREMARK_DAT__FOREIGNLANGACT',
                                      'HREPORTREMARK_DAT__COMMUNICATION');
        } else {
            $field_name_array = array('HREPORTREMARK_DAT__TOTALSTUDYTIME',
                                      'HREPORTREMARK_DAT__COMMUNICATION');
        }

        //通知表所見表示
        $query = knja126pQuery::getHreportRemarkDat($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //総合的な学習の時間
            $extra = "style=\"height:75px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"";
            $row["TOTALSTUDYTIME"] = knjCreateTextArea($objForm, "TOTALSTUDYTIME", 4, 43, "soft", $extra, $row["TOTALSTUDYTIME"]);

            //外国語活動
            $extra = "style=\"height:75px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"";
            $row["FOREIGNLANGACT"] = knjCreateTextArea($objForm, "FOREIGNLANGACT", 4, 43, "soft", $extra, $row["FOREIGNLANGACT"]);

            //学習のようす
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
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja126pSubForm1.html", $arg);
    }
}
?>
