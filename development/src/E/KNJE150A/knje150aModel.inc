<?php

require_once('for_php7.php');


class knje150aModel extends Model {
    var $field          = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    function knje150aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("OUT_DIV"  =>  VARS::post("OUT_DIV"),          //対象ラジオボタン 
                             "DBNAME"   =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd          = VARS::request("cmd");
    }
    /**
     * ＣＳＶ出力
     */
    function getDownloadModel() {
        if ($this->field["OUT_DIV"] == "1") {
            return $this->printStudyrec();
        } else if ($this->field["OUT_DIV"] == "2") {
            return $this->printJview();
        } else {
            return $this->printAttendrec();
        }
    }

    function printStudyrec()
    {
        //DB接続
        $db = Query::dbCheckOut();
        //SQL文発行
        $query = knje150aQuery::getStudyrecCsv($this);
        $result = $db->query($query);
        $headData = array();
        $schregData = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $headData[$row["ANNUAL"]][$row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]] = $row["SUBCLASSNAME"];
            } else {
                $headData[$row["ANNUAL"]][$row["SUBCLASSCD"]] = $row["SUBCLASSNAME"];
            }
            $schregData[$row["SCHREGNO"]]["GRADE"] = $row["GRADE"];
            $schregData[$row["SCHREGNO"]]["HR_CLASS"] = $row["HR_CLASS"];
            $schregData[$row["SCHREGNO"]]["ATTENDNO"] = $row["ATTENDNO"];
            $schregData[$row["SCHREGNO"]]["NAME_SHOW"] = $row["NAME_SHOW"];
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $schregData[$row["SCHREGNO"]]["STUDYREC"][$row["ANNUAL"]][$row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]] = $row["VALUATION"];
            } else {
                $schregData[$row["SCHREGNO"]]["STUDYREC"][$row["ANNUAL"]][$row["SUBCLASSCD"]] = $row["VALUATION"];
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        //並び替え
        ksort($headData);
        foreach ($headData as $anual => $subclassData) {
            ksort($headData[$anual]);
        }

        $nenjiHead = ",,,,,";
        $nenjiSep = "";
        $kamokuHead = "学籍番号,学年,組,出席番号,生徒氏名,";
        $kamokSep = "";
        foreach ($headData as $anual => $subclassData) {
            $nenjiHead .= $anual."年次";
            $nenjiSep = ",";
            foreach ($subclassData as $subCd => $subName) {
                $kamokuHead .= $kamokSep.$subName;
                $nenjiHead .= $nenjiSep;
                $kamokSep = ",";
            }
        }

        $contents  = $nenjiHead."\n";
        $contents .= $kamokuHead."\n";

        //データ
        $data = false;

        foreach ($schregData as $schregNo => $datas) {
            $contents .= $schregNo.",";
            $contents .= $datas["GRADE"].",";
            $contents .= $datas["HR_CLASS"].",";
            $contents .= $datas["ATTENDNO"].",";
            $contents .= $datas["NAME_SHOW"].",";
            foreach ($headData as $anual => $subclassData) {
                foreach ($subclassData as $subCd => $subName) {
                    if ($datas["STUDYREC"][$anual][$subCd]) {
                        $contents .= $datas["STUDYREC"][$anual][$subCd].",";
                    } else {
                        $contents .= ",";
                    }
                }
            }
            $contents .= "\n";

            $data = true;
        }

        //ＣＳＶ
        if ($data) {
            $filename = sprintf("%s.csv", "学習の記録");
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    function printJview()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //観点
        $query = knje150aQuery::getJviewCsv($this);
        $result = $db->query($query);
        $headData = array();
        $headClassData = array();
        $schregData = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $headClassData[$row["ANNUAL"]][$row["VIEWCD"]] = $row["CLASSNAME"];
            $headData[$row["ANNUAL"]][$row["VIEWCD"]] = $row["VIEWNAME"];

            if (strlen($row["VIEWCD"]) > 0) {
                $schregData[$row["SCHREGNO"]]["JVIEW"][$row["ANNUAL"]][$row["VIEWCD"]] = $row["STATUS"];
            }

            $schregData[$row["SCHREGNO"]]["GRADE"] = $row["GRADE"];
            $schregData[$row["SCHREGNO"]]["HR_CLASS"] = $row["HR_CLASS"];
            $schregData[$row["SCHREGNO"]]["ATTENDNO"] = $row["ATTENDNO"];
            $schregData[$row["SCHREGNO"]]["NAME_SHOW"] = $row["NAME_SHOW"];
        }
        $result->free();

        //活動の記録
        $query = knje150aQuery::getBehaviorCsv($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $schregData[$row["SCHREGNO"]]["BEHAVIOR"][$row["ANNUAL"]][$row["CODE"]] = $row["RECORD"];
        }
        $result->free();
        Query::dbCheckIn($db);

        //並び替え
        ksort($headData);
        foreach ($headData as $anual => $viewData) {
            ksort($headData[$anual]);
        }

        $nenjiHead = ",,,,,";
        $nenjiSep = "";
        $kyoukaHead = ",,,,,";
        $befKyouka = "@";
        $viewHead = "学籍番号,学年,組,出席番号,生徒氏名,";
        $viewSep = "";
        $behaviorDiv1 = array("101", "102", "103", "104", "105", "106", "107", "108", "109", "110");
        $behaviorDiv1Name = array("基本的な生活習慣", "健康・体力の向上", "自主・自立", "責任感", "創意工夫", "思いやり・協力", "生命尊重・自然愛護", "勤労・奉仕", "公正・公平", "公共心・心得心");
        $behaviorDiv2 = array("201", "202", "203");
        $behaviorDiv2Name = array("学級活動", "生徒会活動", "学校行事");
        foreach ($headData as $anual => $viewData) {
            $nenjiHead .= $anual."年次";
            $nenjiSep = ",";
            foreach ($viewData as $viewCd => $viewName) {
                if (strlen($viewCd) == 0) {
                    continue;
                }
                $kyoukaHead .= $befKyouka != $headClassData[$anual]["$viewCd"] ? $viewSep.$headClassData[$anual]["$viewCd"] : $viewSep;
                $viewHead .= $viewSep.$viewName;
                $nenjiHead .= $nenjiSep;
                $viewSep = ",";
                $befKyouka = $headClassData[$anual]["$viewCd"] ? $headClassData[$anual]["$viewCd"] : $befKyouka;
            }

            for ($behavior = 0; $behavior < get_count($behaviorDiv1); $behavior++) {
                $kyoukaHead .= $behavior == 0 ? $viewSep."行動の記録" : $viewSep;
                $viewHead .= $viewSep.$behaviorDiv1Name[$behavior];
                $nenjiHead .= $nenjiSep;
                $viewSep = ",";
            }
            for ($behavior = 0; $behavior < get_count($behaviorDiv2); $behavior++) {
                $kyoukaHead .= $behavior == 0 ? $viewSep."特別活動" : $viewSep;
                $viewHead .= $viewSep.$behaviorDiv2Name[$behavior];
                $nenjiHead .= $nenjiSep;
                $viewSep = ",";
            }

        }

        $contents  = $nenjiHead."\n";
        $contents .= $kyoukaHead."\n";
        $contents .= $viewHead."\n";

        //データ
        $data = false;

        foreach ($schregData as $schregNo => $datas) {
            $contents .= $schregNo.",";
            $contents .= $datas["GRADE"].",";
            $contents .= $datas["HR_CLASS"].",";
            $contents .= $datas["ATTENDNO"].",";
            $contents .= $datas["NAME_SHOW"].",";
            foreach ($headData as $anual => $viewData) {
                foreach ($viewData as $viewCd => $viewName) {
                    if (strlen($viewCd) == 0) {
                        continue;
                    }
                    if ($datas["JVIEW"][$anual][$viewCd]) {
                        $contents .= $datas["JVIEW"][$anual][$viewCd].",";
                    } else {
                        $contents .= ",";
                    }
                }
                for ($behavior = 0; $behavior < get_count($behaviorDiv1); $behavior++) {
                    if ($datas["BEHAVIOR"][$anual][$behaviorDiv1[$behavior]]) {
                        $contents .= $datas["BEHAVIOR"][$anual][$behaviorDiv1[$behavior]].",";
                    } else {
                        $contents .= ",";
                    }
                }
                for ($behavior = 0; $behavior < get_count($behaviorDiv2); $behavior++) {
                    if ($datas["BEHAVIOR"][$anual][$behaviorDiv2[$behavior]]) {
                        $contents .= $datas["BEHAVIOR"][$anual][$behaviorDiv2[$behavior]].",";
                    } else {
                        $contents .= ",";
                    }
                }
            }

            $contents .= "\n";

            $data = true;
        }

        //ＣＳＶ
        if ($data) {
            $filename = sprintf("%s.csv", "観点の記録");
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    function printAttendrec()
    {
        //DB接続
        $db = Query::dbCheckOut();
        //SQL文発行
        $query = knje150aQuery::getAttendrecCsv($this);
        $result = $db->query($query);
        $dataTemp = array();
        $tempCnt = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            //学籍
            if ($dataTemp[$row["SCHREGNO"]] == "") {
                $dataTemp[$row["SCHREGNO"]][] = $row["SCHREGNO"];
                $dataTemp[$row["SCHREGNO"]][] = $row["GRADE"];
                $dataTemp[$row["SCHREGNO"]][] = $row["HR_CLASS"];
                $dataTemp[$row["SCHREGNO"]][] = $row["ATTENDNO"];
                $dataTemp[$row["SCHREGNO"]][] = $row["NAME_SHOW"];
            }
            //記録データ
            $dataTemp[$row["SCHREGNO"]][] = $row["ANNUAL"];
            $dataTemp[$row["SCHREGNO"]][] = $row["LESSON"];
            $dataTemp[$row["SCHREGNO"]][] = $row["SUSPEND_MOURNING"];
            $dataTemp[$row["SCHREGNO"]][] = $row["ABROAD"];
            $dataTemp[$row["SCHREGNO"]][] = $row["REQUIREPRESENT"];
            $dataTemp[$row["SCHREGNO"]][] = $row["PRESENT"];
            $dataTemp[$row["SCHREGNO"]][] = $row["ABSENT"];

            $tempCnt[$row["SCHREGNO"]]++;
        }
        $result->free();
        Query::dbCheckIn($db);
        //ヘッダ
        $header = array("SCHREGNO"     => "学籍番号",
                        "GRADE"        => "学年",
                        "HR_CLASS"     => "組",
                        "ATTENDNO"     => "出席番号",
                        "NAME_SHOW"    => "生徒氏名"
                        );
        $headCnt = 0;
        foreach ($tempCnt as $key => $val) {
            $headCnt = $headCnt < $val ? $val : $headCnt;
        }
        for ($i = 0; $i < $headCnt; $i++) {
            $header["ANNUAL".$i]            = "年次";
            $header["LESSON".$i]            = "授業日数";
            $header["SUSPEND_MOURNING".$i]  = "出席停止・忌引等の日数";
            $header["ABROAD".$i]            = "留学中の授業日数";
            $header["REQUIREPRESENT".$i]    = "出席しなければならない日数";
            $header["PRESENT".$i]           = "出席日数";
            $header["ABSENT".$i]            = "欠席日数";
        }
        $contents = implode($header, ",") ."\n";
        //データ
        $data = false;
        foreach ($dataTemp as $key => $val) {
            $contents .= implode($val, ",") ."\n";
            $data = true;
        }
        //ＣＳＶ
        if ($data) {
            $filename = sprintf("%s.csv", "出欠の記録");
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
