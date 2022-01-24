<?php

require_once("for_php7.php");

//ビュー作成用クラス
class knja125jSubForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();
        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knja125jindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //各項目セット
        if ($model->Properties["J_tutihyosanshou_MAX_CNT"] &&
            $model->Properties["J_tutihyosanshou_MAX_CNT"] > 0) {
            $colspan_count = 0;
            $dataCnt = $model->Properties["J_tutihyosanshou_MAX_CNT"];
            $dispDataArray = array();
            //項目名表示
            for ($labelCnt = 1; $labelCnt <= $dataCnt; $labelCnt++) {
                if (!$model->Properties["J_tutihyosanshou:{$labelCnt}"]) {
                    continue;
                }
                // 例：J_tutihyosanshou:1 = HREPORTREMARK_DETAIL_DAT@REMARK1@01:01@学習活動1
                list($tableName, $fieldName, $whereVal, $titleName) = explode("@", $model->Properties["J_tutihyosanshou:{$labelCnt}"]);
                $dispDataArray[$labelCnt]["TABLE"] = $tableName;
                $dispDataArray[$labelCnt]["FIELD"] = $fieldName;
                $dispDataArray[$labelCnt]["WHERE"] = $whereVal;
                $dispDataArray[$labelCnt]["TITLE"] = $titleName;

                $arg["LABELS"][] = array("LABEL" => $titleName);
                $colspan_count++;
            }

            $arg["colspan"] = $colspan_count;

            //通知表所見表示
            $semFlg = "";
            $setRow = $semeName = array();
            $query = knja125jQuery::getSemester($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $semeName[$row["SEMESTER"]] = $row["SEMESTERNAME"];
            }
            $result->free();

            //学期＋データセット
            foreach ($semeName as $semester => $semesterName) {
                $sub_arg = array();
                $sub_arg["datas"][] = array("DATA" => $semesterName);
                foreach ($dispDataArray as $dataSeq => $dispData) {
                    if ($dispData["TABLE"] == "HREPORTREMARK_DAT") {
                        $query = knja125jQuery::getHreportremarkDatKahen($model, $semester, $dispData);
                    } else {
                        $query = knja125jQuery::getHreportremarkDetailDatKahen($model, $semester, $dispData);
                    }
                    $setVal = $db->getOne($query);
                    $extra = "style=\"height:75px; white-space: pre-wrap;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"";
                    $setTextArea = knjCreateTextArea($objForm, $filed_name, 4, 43, "soft", $extra, $setVal);
                    $sub_arg["datas"][] = array("DATA" => $setTextArea);
                }
                $arg["data_array"][] = $sub_arg;
            }
        } else {
            //表示項目
            $field_name_array = array('HREPORTREMARK_DAT__TOTALSTUDYTIME',
                                      'HREPORTREMARK_DAT__COMMUNICATION');

            //通知表所見表示
            $query = knja125jQuery::getHreportRemarkDat($model);
            $result = $db->query($query);
            while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //総合的な学習の時間
                $extra = "style=\"height:75px; white-space: pre-wrap;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"";
                $row["TOTALSTUDYTIME"] = knjCreateTextArea($objForm, "TOTALSTUDYTIME", 4, 43, "soft", $extra, $row["TOTALSTUDYTIME"]);

                //担任からの所見
                $extra = "style=\"height:75px; white-space: pre-wrap;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"";
                $row["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", 4, 43, "soft", $extra, $row["COMMUNICATION"]);

                $sub_arg = array();
                $sub_arg["datas"][] = array("DATA" => $row["SEMESTERNAME"]);
                foreach ($model->Properties as $key => $val) {
                    if (in_array($key, $field_name_array) && strlen($val)) {
                        list($table_name, $filed_name) = preg_split('/__/', $key);
                        $sub_arg["datas"][] = array("DATA" => $row[$filed_name]);
                    }
                }

                $arg["data_array"][] = $sub_arg;
            }
            $colspan_count = 0;
            //項目名表示
            foreach ($model->Properties as $key => $val) {
                if (in_array($key, $field_name_array) && strlen($val)) {
                    list($table_name, $filed_name) = preg_split('/__/', $key);
                    $arg["LABELS"][] = array("LABEL" => $val);
                    $colspan_count++;
                }
            }

            $arg["colspan"] = $colspan_count;
        }

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
        View::toHTML($model, "knja125jSubForm1.html", $arg);
    }
}
