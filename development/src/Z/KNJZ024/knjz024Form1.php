<?php

require_once('for_php7.php');

class knjz024form1 {
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();
        if ($model->dataBaseinfo === '2') $db2 = Query::dbCheckOut2();
        $arg["start"] = $objForm->get_start("main", "POST", "knjz024index.php", "", "main");
        $arg["Closing"] = "";

        /**************************/
        /* セキュリティーチェック */
        /**************************/
        if(AUTHORITY != DEF_UPDATABLE) {
            $arg["Closing"] = " closing_window(); ";
        }

        //学校名称2表示
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $info = $db->getRow(knjz024Query::getSchoolName2($model), DB_FETCHMODE_ASSOC);
            $arg["SCH_NAME2"] = (strlen($info["SCHOOLNAME2"]) > 0) ? "<<".$info["SCHOOLNAME2"].">>" : "";
        }

        //学期数取得
        $semester = $db->getOne(knjz024Query::getSemester($model, "max"));
        knjCreateHidden($objForm, "MAX_SEMESTER", $semester);

        //課程学科
        $query = knjz024Query::getCourseMajor($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSE_MAJOR"], "COURSE_MAJOR", $extra, 1, "BLANK");

        $month_syusu = array();
        if ($model->Properties["use_Month_Syusu"] == "1") {
            //月別週数データ取得
            $query = knjz024Query::getAttendSyusuMst($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $month_syusu["HOUTEI_SYUSU_MONTH_".$row["SEMESTER"]."-".$row["MONTH"]] = $row["SYUSU"];
            }
            $result->free();
        }

        if ($model->cmd == "chenge") {
            $Row = $model->field;
            //法定時数の学期制授業週数がNULLの場合再セット（実時数の時、フィールドがなくなるため）
            $query = knjz024Query::getSchoolDetailDat($model);
            $HouteiRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            for ($sem = 1; $sem <= $semester; $sem++) {
                if (!$Row["HOUTEI_SYUSU_SEMESTER".$sem]) {
                    $Row["HOUTEI_SYUSU_SEMESTER".$sem] = $HouteiRow["HOUTEI_SYUSU_SEMESTER".$sem];
                }
            }
            if (!isset($model->warning)) {
                foreach ($month_syusu as $key => $val) $Row[$key] = $val;
            }
        } else {
            $query = knjz024Query::getSchoolDetailDat($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            foreach ($month_syusu as $key => $val) $Row[$key] = $val;
        }

        /********/
        /* 年度 */
        /********/
        $arg["YEAR"] = $model->year;

        /******************/
        /* コンボボックス */
        /******************/
        //授業数管理区分
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        //V_名称マスタ「Z042」が1件でもあれば、名称マスタを表示する。
        $query = knjz024Query::getVNameMstZ042($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        //なければ、固定で表示する。
        if (get_count($opt) == 1) {
            $opt[] = array('label' => '1:法定時数', 'value' => '1');
            $opt[] = array('label' => '2:実時数',   'value' => '2');
        }
        $extra = "onchange=\"return btn_jisuchange('chenge');\"";
        $arg["data"]["JUGYOU_JISU_FLG"] = knjCreateCombo($objForm, "JUGYOU_JISU_FLG", $Row["JUGYOU_JISU_FLG"], $opt, $extra, 1);
        //授業数管理区分
        $opt = array();
        $opt[] = array('label' => '1:四捨五入', 'value' => '1');
        $opt[] = array('label' => '2:切り上げ', 'value' => '2');
        $opt[] = array('label' => '3:切り捨て', 'value' => '3');
        $opt[] = array('label' => '4:実数',     'value' => '4');
        if (strlen($Row["JOUGENTI_SANSYUTU_HOU"])) {
            $value = $Row["JOUGENTI_SANSYUTU_HOU"];
        } else {
            if (preg_match('/[3-4]/', $Row["ABSENT_COV"])) {
                $value = 4;
            } else {
                $value = 3;
            }
        }
        $extra = "";
        $arg["data"]["JOUGENTI_SANSYUTU_HOU"] = knjCreateCombo($objForm, "JOUGENTI_SANSYUTU_HOU", $value, $opt, $extra, 1);

        /********************/
        /* テキストボックス */
        /********************/
        //履修上限値(分子) (通常)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RISYU_BUNSI"] = knjCreateTextBox($objForm, $Row["RISYU_BUNSI"], "RISYU_BUNSI", 2, 2, $extra);
        //履修上限値(分母) (通常)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RISYU_BUNBO"] = knjCreateTextBox($objForm, $Row["RISYU_BUNBO"], "RISYU_BUNBO", 2, 2, $extra);
        //修得上限値(分子) (通常)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SYUTOKU_BUNSI"] = knjCreateTextBox($objForm, $Row["SYUTOKU_BUNSI"], "SYUTOKU_BUNSI", 2, 2, $extra);
        //修得上限値(分母) (通常)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SYUTOKU_BUNBO"] = knjCreateTextBox($objForm, $Row["SYUTOKU_BUNBO"], "SYUTOKU_BUNBO", 2, 2, $extra);
        //履修上限値(分子) (特活)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RISYU_BUNSI_SPECIAL"] = knjCreateTextBox($objForm, $Row["RISYU_BUNSI_SPECIAL"], "RISYU_BUNSI_SPECIAL", 2, 2, $extra);
        //特活上限値(分母) (特活)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RISYU_BUNBO_SPECIAL"] = knjCreateTextBox($objForm, $Row["RISYU_BUNBO_SPECIAL"], "RISYU_BUNBO_SPECIAL", 2, 2, $extra);
        //修得上限値(分母) (特活)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SYUTOKU_BUNBO_SPECIAL"] = knjCreateTextBox($objForm, $Row["SYUTOKU_BUNBO_SPECIAL"], "SYUTOKU_BUNBO_SPECIAL", 2, 2, $extra);
        //授業時間(分)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["JITU_JIFUN"] = knjCreateTextBox($objForm, $Row["JITU_JIFUN"], "JITU_JIFUN", 3, 3, $extra);
        //特活授業時間(分)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["JITU_JIFUN_SPECIAL"] = knjCreateTextBox($objForm, $Row["JITU_JIFUN_SPECIAL"], "JITU_JIFUN_SPECIAL", 3, 3, $extra);
        //授業週数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["JITU_SYUSU"] = knjCreateTextBox($objForm, $Row["JITU_SYUSU"], "JITU_SYUSU", 3, 3, $extra);
        //欠席日数注意(分子)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["KESSEKI_WARN_BUNSI"] = knjCreateTextBox($objForm, $Row["KESSEKI_WARN_BUNSI"], "KESSEKI_WARN_BUNSI", 2, 2, $extra);
        //欠席日数注意(分母)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["KESSEKI_WARN_BUNBO"] = knjCreateTextBox($objForm, $Row["KESSEKI_WARN_BUNBO"], "KESSEKI_WARN_BUNBO", 2, 2, $extra);
        //欠席日数超過(分子)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["KESSEKI_OUT_BUNSI"] = knjCreateTextBox($objForm, $Row["KESSEKI_OUT_BUNSI"], "KESSEKI_OUT_BUNSI", 2, 2, $extra);
        //欠席日数超過(分母)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["KESSEKI_OUT_BUNBO"] = knjCreateTextBox($objForm, $Row["KESSEKI_OUT_BUNBO"], "KESSEKI_OUT_BUNBO", 2, 2, $extra);

        //法定授業週数
        $tmp = array();
        $pad_top = 70;
        if ($Row["JUGYOU_JISU_FLG"] === '1' && $model->Properties["hibiNyuuryokuNasi"] === '1') {
            if ($model->Properties["use_Month_Syusu"] == "1") {
                //月一覧取得
                $m_data = array();
                $query = knjz024Query::getMonthList($model);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $m_data[$row["NAMECD2"]] = $row["NAME1"];
                }
                $result->free();

                //月別週数
                for ($dcnt = 0; $dcnt < get_count($model->seminfo); $dcnt++) {
                    for ($i = $model->seminfo[$dcnt]["S_MONTH"]; $i <= $model->seminfo[$dcnt]["E_MONTH"]; $i++) {
                        $month = $i;
                        if ($i > 12) $month = $i - 12;
                        $month = sprintf('%02d', $month);

                        if (strlen($m_data[$month]) > 0) {
                            $extra = "onblur=\"this.value=toInteger(this.value)\"";
                            $tmp["HOUTEI_SYUSU_MONTH"] = knjCreateTextBox($objForm, $Row["HOUTEI_SYUSU_MONTH_".$model->seminfo[$dcnt]["SEMESTER"]."-".$month], "HOUTEI_SYUSU_MONTH_".$model->seminfo[$dcnt]["SEMESTER"]."-".$month, 3, 3, $extra);
                            $tmp["SYUSU_LABEL"] = $m_data[$month]." (".$model->seminfo[$dcnt]["SEMESTERNAME"].") 週数";
                            $arg["m_syusu"][] = $tmp;
                            $pad_top = 10;
                        }
                    }
                }
            } else {
                //学期別週数
                $query = knjz024Query::getSemester($model);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $extra = "onblur=\"this.value=toInteger(this.value)\"";
                    $tmp["HOUTEI_SYUSU_SEMESTER"] = knjCreateTextBox($objForm, $Row["HOUTEI_SYUSU_SEMESTER".$row["SEMESTER"]], "HOUTEI_SYUSU_SEMESTER".$row["SEMESTER"], 3, 3, $extra);
                    $tmp["SEMESTER_NAME"] = $row["SEMESTERNAME"];
                    $arg["syusu"][] = $tmp;
                }
            }
        }

        $arg["padding-top"] = $pad_top."px";

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onClick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onClick=\"return showConfirm();\"";
        $arg["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "取 消", $extra);
        //終了
        $extra = "onClick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "TOKUBETU_KATUDO_KANSAN", $Row["TOKUBETU_KATUDO_KANSAN"]);
        knjCreateHidden($objForm, "AMARI_KURIAGE", $Row["AMARI_KURIAGE"]);
        knjCreateHidden($objForm, "dataBaseinfo", $model->dataBaseinfo);

        Query::dbCheckIn($db);
        if ($model->dataBaseinfo === '2') Query::dbCheckIn($db2);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz024Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
