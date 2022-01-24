<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd501kForm1.php 57471 2017-12-13 07:53:28Z yamashiro $
class knjd501kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd501kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //処理年度表示
        $arg["TOP"]["CONFIRMATION"] = CTRL_YEAR;

        //学年コンボ
        $query = knjd501kQuery::get_grade_data();
        $this->makeCombo($objForm, $arg, $db, $model, "SHOW_GRADE", "GRADE", $model->grade, "GRADE", $query, "", "");

        //学期コンボ
        $query = knjd501kQuery::get_semester_data();
        $this->makeCombo($objForm, $arg, $db, $model, "SEMESTERNAME", "SEMESTER", $model->semes, "SEMES", $query, "", "");

        //科目コンボ
        $query = knjd501kQuery::get_subclass_data($model);
        $this->makeCombo($objForm, $arg, $db, $model, "LABEL", "VALUE", $model->subcd, "SUBCD", $query, "BLANK", "ALL");

        //メインデータ取得
        if ($model->grade && $model->semes && $model->subcd != "") {
            $this->makeMain($objForm, $arg, $db, $model);
        }

        //ボタン作成
        $this->makeButton($objForm, $arg, $model);

        //hiddenを作成
        $this->makeHidden($objForm, $model, $inputableCount, $txt_cnt);

        Query::dbCheckIn($db);

        //処理が完了、又は権限が無ければ閉じる。
        if(CTRL_YEAR == ""){
            $arg["Closing"] = "  closing_window('year'); " ;
        }else if($model->sec_competence == DEF_NOAUTH){
            $arg["Closing"] = "  closing_window('cm'); " ;
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd501kForm1.html", $arg);
    }

    //各コンボ作成
    function makeCombo(&$objForm, &$arg, $db, $model, $label, $value, &$val, $name, $query, $blank, $all)
    {
        $opt = array();
        //空リストをセット
        if ($blank == "BLANK") {
            $opt[] = array("label" => "", "value" => "");
        }
        //全てをセット
        if ($all == "ALL") {
            $opt[] = array("label" => "全て", "value" => "0");
        }

        $result = $db->query($query);
        while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $Row[$label], "value" => $Row[$value]);
        }
        $result->free();

        $val = ($val == "") ? $opt[0]["value"] : $val;

        $arg["TOP"][$name] = $this->createCombo($objForm, $name, $val, $opt, "onChange=\"return btn_submit('');\"", 1);
    }

    //メインデータ作成
    function makeMain(&$objForm, &$arg, $db, &$model)
    {
        $kk_ks_field = array(); //SQL抽出条件用

        //データ抽出条件SQL作成
        $this->makeWhere_kkks($db, $model, $kk_ks_field);

        //メインSQL作成
        $sql = $this->makeMaintData($kk_ks_field, $db, $model);

        if (is_array($sql)) {
            for ($i = 0; $i < get_count($sql); $i++) {
                $result = $db->query($sql[$i]);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $mainData[$row["HR_SHOW"].$row["SUBCLASSCD"]] = $row;
                }
                $result->free();
            }
            if (is_array($mainData)) {
                ksort($mainData);
                foreach ($mainData as $key => $val) {
                    //背景色取得
                    $val["BG"] = $this->getBgColor($db, $val["SCHREGNO"]);

                    //データ作成
                    $this->makeData($objForm, $arg, $model, $val, $idcnt);
                    $idcnt++;
                }
            }
        }
    }

    //データ抽出条件SQL作成
    function makeWhere_kkks($db, &$model, &$kk_ks_field)
    {
        for ($sem = 1; $sem <= $model->semes; $sem++) {
            $result = $db->query(knjd501kQuery::get_testcnt($model, $sem));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $and = "";
                if ($kk_ks_field[$row["SUBCD"]] != "") {
                    $and = " AND ";
                }
                $kk_ks_field[$row["SUBCD"]] .= $this->setField($row, $model, $and);
            }
            $result->free();
        }
    }

    //SQLセット
    function setField($row, &$model, $and)
    {
        $field = "";
        $in    = " IN ('KK','KS')";
        $fieldName = "SEM".$row["SEMES"];

        //TESTSUM：１=中間、２=期末、３=中間＋期末
        if ($row["MAXTEST"] == $row["MINTEST"]) {
            if ($row["MAXTEST"] == "1") {
                $field = $and.$fieldName."_INTER_REC_DI".$in;
            } else if ($row["MAXTEST"] == "2") {
                $field = $and.$fieldName."_TERM_REC_DI".$in;
            }
        } else {
            $field  = $and.$fieldName."_INTER_REC_DI".$in;
            $field .= " AND ".$fieldName."_TERM_REC_DI".$in;
        }
        return $field;
    }

    //メインデータ作成
    function makeMaintData($kk_ks_field, $db, &$model)
    {
        foreach ($kk_ks_field as $key => $val) {
            //配列にデータを保持する。
            if (0 < ($check = $db->getOne(knjd501kQuery::get_mainData($model, $key, $val, "WITH A AS (", " ) SELECT COUNT(*) FROM A")))) {
                $sql[] = knjd501kQuery::get_mainData($model, $key, $val, "","");
            }
        }
        return $sql;
    }

    //背景色取得
    function getBgColor($db, $schregno)
    {
        //在籍期間(入学月日、卒業月日)の取得
        $grd_cnt  = $db->getOne(knjd501kQuery::getOnTheRegisterPeriod($schregno));
        //異動者情報の取得
        $tans_cnt = $db->getOne(knjd501kQuery::getTransferData($schregno));
        if (0 < $grd_cnt || 0 < $tans_cnt) {
            return "yellow";
        }
        return "white";
    }

    //表示データ作成
    function makeData(&$objForm, &$arg, &$model, $row, $idcnt)
    {
        $showData = "SEM".$model->semes;
        if ($row[$showData."_REC"] == "" || $row[$showData."_REC_FLG"] == "1") {
            //テキスト作成
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $row[$showData."_REC"] = $this->createText($objForm, $showData."_REC".$idcnt, $row[$showData."_REC"], $extra, 3, 3);

            //チェックボックス作成
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $row["CHK_BOX"] = $this->createCheckBox($objForm, "CHK_BOX", $idcnt."-".$row["SCHREGNO"]."-".$row["CLASSCD"].':'.$row["SCHOOL_KIND"].':'.$row["CURRICULUM_CD"].':'.$row["SUBCLASSCD"], "checked", 1);
            } else {
	            $row["CHK_BOX"] = $this->createCheckBox($objForm, "CHK_BOX", $idcnt."-".$row["SCHREGNO"]."-".$row["SUBCLASSCD"], "checked", 1);
        	}
        } else {
            return ;
        }
        for ($cnt = 1; $cnt <= $model->semes; $cnt++) {
            if ($row["SEM".$cnt."_REC"] != "" && $row["SEM".$cnt."_REC_FLG"] == 1) {
                $row["SEM".$cnt."_COLOR_S"] = "<font color=\"#00CC00\">";
                $row["SEM".$cnt."_COLOR_E"] = "</font>";
            }
        }
        $arg["data"][] = $row;
    }

    //ボタン作成
    function makeButton(&$objForm, &$arg, $model)
    {
        //保存ボタン
        $arg["BUTTON"]["BTN_PRSV"] = $this->createBtn($objForm, "btn_prsv", " 保 存 ", " onClick=\"return btn_submit('update');\"");
        //取消ボタン
        $arg["BUTTON"]["BTN_CAN"] = $this->createBtn($objForm, "btn_can", " 取 消 ", " onClick=\"return btn_submit('cancel');\"");
        //終了ボタン
        $arg["BUTTON"]["BTN_END"] = $this->createBtn($objForm, "btn_end", " 終 了 ", " onClick=\"return closeWin();\"");
    }

    //hidden作成
    function makeHidden(&$objForm, $model, $inputableCount, $txt_cnt)
    {
        //コマンド
        $objForm->ae($this->createHiddenAe("cmd"));
        //補点データ数カウント
        $objForm->ae($this->createHiddenAe("dataCount", $inputableCount));
        //入力可能テキスト
        $objForm->ae($this->createHiddenAe("txtCount", $txt_cnt));
    }

    //コンボ作成
    function createCombo(&$objForm, $name, $value, $options, $extra, $size)
    {
        $objForm->ae( array("type"      => "select",
                            "name"      => $name,
                            "size"      => $size,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "options"   => $options));
        return $objForm->ge($name);
    }

    //チェックボックス作成
    function createCheckBox(&$objForm, $name, $value, $extra, $multi)
    {

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "multiple"  => $multi));

        return $objForm->ge($name);
    }

    //テキスト作成
    function createText(&$objForm, $name, $value, $extra, $size, $maxlen)
    {
        $objForm->ae( array("type"      => "text",
                            "name"      => $name,
                            "size"      => $size,
                            "maxlength" => $maxlen,
                            "extrahtml" => $extra,
                            "value"     => $value));
        return $objForm->ge($name);
    }

    //ボタン作成
    function createBtn(&$objForm, $name, $value, $extra)
    {
        $objForm->ae( array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ) );
        return $objForm->ge($name);
    }

    //Hidden作成ae
    function createHiddenAe($name, $value = "")
    {
        $opt_hidden = array();
        $opt_hidden = array("type"      => "hidden",
                            "name"      => $name,
                            "value"     => $value);
        return $opt_hidden;
    }

}

?>
