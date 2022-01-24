<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja125pSubForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();
        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knja125pindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //益城の仕様チェック
        if ($model->Properties["useCheckForeignlangact"] == "1") {
            $chk_gdat = $db->getOne(knja125pQuery::checkSchregRegdGdat($model));
            if ($chk_gdat) {
                $field_name_array = array('HREPORTREMARK_DAT__TOTALSTUDYTIME',
                                          'HREPORTREMARK_DAT__FOREIGNLANGACT',
                                          'HREPORTREMARK_DAT__COMMUNICATION',
                                          'HREPORTREMARK_DAT__REMARK3');
            } else {
                $field_name_array = array('HREPORTREMARK_DAT__TOTALSTUDYTIME',
                                          'HREPORTREMARK_DAT__COMMUNICATION',
                                          'HREPORTREMARK_DAT__REMARK3');
            }
        } else {
            $field_name_array = array('HREPORTREMARK_DAT__TOTALSTUDYTIME',
                                      'HREPORTREMARK_DAT__COMMUNICATION',
                                      'HREPORTREMARK_DAT__REMARK3');
        }

        // ※プロパティだが、雲雀丘専用の意味で作成
        if ($model->Properties["useKnja125pBehaviorSemesMst"] == '2') {
            $arg["useKnja125pBehaviorSemesMst_2"] = '1';

            //年次取得
            $gradeCd = $db->getOne(knja125pQuery::getGradeCdSubform1($model));

            //統合的な学習
            if ($gradeCd > 2) {
                $arg["sougou"] = "1";
                $query = knja125pQuery::getTotalStudyText($model);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //textArea
                    $extra = "";
                    $row["SUB_CONTENT1"] = knjCreateTextArea($objForm, "MORAL".$row["SEMESTER"], "1", "25", "wrap", $extra, $row["MORAL"]);

                    $row["TITLE"]  = knjCreateTextBox($objForm, $row["TITLE"], "DOUGOU_TITLE", 30, 30, $extra);
                    $row["TEXT01"] = knjCreateTextArea($objForm, "S_TEXT01".$row["SEMESTER"], "4", "60", "wrap", $extra, $row["TEXT01"]);

                    $arg["data3"][] = $row;
                }
            }

            //道徳
            $arg["moral"] = "1";
            $moralCol = $gradeCd > 2 ? '60': '56';
            $query = knja125pQuery::getMoralText($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //textArea
                $extra = "";
                $row["TITLE"]  = knjCreateTextBox($objForm, $row["TITLE"], "MORAL_TITLE", 50, 50, $extra);
                $row["TEXT01"] = knjCreateTextArea($objForm, "M_TEXT01".$row["SEMESTER"], "4", $moralCol, "wrap", $extra, $row["TEXT01"]);

                $arg["data4"][] = $row;
            }

            //特別活動・クラブ活動
            if ($gradeCd > 4) {
                $arg["club"] = "1";
                $clubArr = array();
                $query = knja125pQuery::getActClubText($model);
                $clubArr = $db->getRow($query, DB_FETCHMODE_ASSOC);
                //textArea
                $extra = "";
                $arg["data5"]["TEXT01"]  = knjCreateTextBox($objForm, $clubArr["TEXT01"], "C_TEXT01", 22, 22, $extra);
                $arg["data5"]["TEXT02"]  = knjCreateTextBox($objForm, $clubArr["TEXT02"], "C_TEXT02", 22, 22, $extra);
                $arg["data5"]["TEXT03"]  = knjCreateTextBox($objForm, $clubArr["TEXT03"], "C_TEXT03", 26, 26, $extra);
            }

            //特記事項・担任からの通信
            $query = knja125pQuery::getHreportRemarkDat2($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $arg["useTCTTL"] = '1';
                $row["REMARK_TCTTL"] = knjCreateTextArea($objForm, "REMARK_TCTTL".$row["SEMESTER"], "10", "50", "wrap", $extra, $row["REMARK_TCTTL"]);

                //textArea
                $extra = "";
                $row["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION".$row["SEMESTER"], "10", "50", "wrap", $extra, $row["COMMUNICATION"]);

                $arg["data2"][] = $row;
            }
        } else {
            $arg["Not_useKnja125pBehaviorSemesMst_2"] = '1';
        }

        //通知表所見表示
        $query = knja125pQuery::getHreportRemarkDat($model);
        $result = $db->query($query);
        while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //特別活動
            $extra = "style=\"height:75px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"";
            $row["REMARK3"] = knjCreateTextArea($objForm, "REMARK3", 4, 43, "soft", $extra, $row["REMARK3"]);

            //総合的な学習の時間 OR 明小タイム
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
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja125pSubForm1.html", $arg);
    }
}
