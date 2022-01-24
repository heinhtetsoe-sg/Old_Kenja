<?php

require_once('for_php7.php');
class knji100c_07Model extends Model {

    var $cmd;
    var $schregno;                  //学籍番号
    var $control_data = array();    //コントロールマスタデータを保持
    var $item;
    var $selectdata_l;                //対象生徒
    var $selectdata_r;                //書出し項目一覧
    var $mode;                      //生徒区分（在校生・卒業生）

    function knja150Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }

        $this->item = array();
        $this->item[]["SCHREG_STUDYREC_DAT"]    = "学習記録データ";
        $this->item[]["SCHREG_ATTENDREC_DAT"]   = "出席データ(進学用)";
        $this->item[]["HTRAINREMARK_DAT"]       = "所見データ(進学用)";
        $this->item[]["SCHREG_ATTENDREC_DAT2"]  = "出席データ(就職用)";
        $this->item[]["HTRAINREMARK_DAT2"]      = "所見データ(就職用)";

        //ITEM
        $this->selectdata_r     = VARS::post("selectdata_r");
        //対象生徒
        $this->selectdata_l     = VARS::post("selectdata_l");
        //課程学科
        $this->coursemajor      = VARS::post("COURSEMAJOR");
        //学年
        $this->grade            = VARS::post("GRADE");
        //性別
        $this->sex              = VARS::post("SEX");
        //年組
        $this->grade_hr_class   = VARS::post("GRADE_HR_CLASS");
        //異動対象日付
        $this->date             = VARS::post("DATE");
        //出力設定
        $this->output           = VARS::post("OUTPUT");

        //サブシステム
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '07';
        //対象生徒
        if(VARS::get("selectdata_l"))
            $this->selectdata_l     = VARS::get("selectdata_l");
        //学年
        if(VARS::get("GRADE"))
            $this->grade   = VARS::get("GRADE");
        //年組
        if(VARS::get("GRADE_HR_CLASS"))
            $this->grade_hr_class   = VARS::get("GRADE_HR_CLASS");
        //出力設定
        if(VARS::get("OUTPUT"))
            $this->output = VARS::get("OUTPUT");
    }

    //CSV書出し
    function &getCsvModel() {

        if ($this->selectdata_r == "SCHREG_STUDYREC_DAT") {
            return $this->getStudyRecCsv();
        } else if ($this->selectdata_r == "SCHREG_ATTENDREC_DAT" || $this->selectdata_r == "SCHREG_ATTENDREC_DAT2") {
            return $this->getAttendCsv();
        } else {
            return $this->getHexamCsv();
        }

    }

    //CSV書出し
    function getStudyRecCsv() {
        $db = Query::dbCheckOut();
        $item = explode(",", $this->selectdata_r);

        $query = knji100c_07Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_07Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }

        $yearSubclass = array();
        $query = knji100c_07Query::selectSubclassTitleQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $yearSubclass[$row["YEAR"]][$row["SUBCLASSCD"]] = "";
        }

        $contents = "";
        $subTitle = "";
        //ヘッダ作成
        if ($this->output == OUT_CODE_NAME) {
            $contents .= ",,,,,,";
            $subTitle .= "学籍番号,氏名,学年,組,出席番号,性別コード,性別";
        } else {
            $contents .= ",,,,,";
            $subTitle .= "学籍番号,氏名,学年,組,出席番号,性別";
        }
        foreach ($yearSubclass as $year => $subclass) {
            $firstFlg = true;
            foreach ($subclass as $subCd => $val) {
                $setYear = $firstFlg ? $year."年度" : "";
                $contents .= ",{$setYear},,,";
                $subTitle .= ",{$subCd},成績,履修単位,修得単位";
                $firstFlg = false;
            }
        }
        $contents .= "\n";
        $contents .= $subTitle;
        $contents .= "\n";

        $schArray = explode(",", $this->selectdata_l);
        foreach ($schArray as $key => $schregno) {
            $headFlg = true;
            $sep = "";
            if (get_count($yearSubclass) > 0) {
                foreach ($yearSubclass as $year => $subclass) {
                    foreach ($subclass as $subCd => $val) {
                        $query = knji100c_07Query::selectStudyRecQuery($this, $year, $subCd, $schregno, $headFlg);
                        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                        $contents .= $sep.implode($row, ",");
                        $headFlg = false;
                        $sep = ",";
                    }
                }
            } else {
                $query = knji100c_07Query::selectStudyRecQuery($this, $year, $subCd, $schregno, $headFlg);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                unset($row['SUBCLASSNAME']);
                unset($row['VALUATION']);
                unset($row['COMP_CREDIT']);
                unset($row['GET_ADD']);
                unset($row['REMARK']);
                $contents .= $sep.implode($row, ",");
                $headFlg = false;
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }

        Query::dbCheckIn($db);
        if ($data){
            $filename = sprintf("%s.csv","指導要録(学習記録データ)");
            common::downloadContents($contents, $filename);
            exit;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CSV書出し
    function getAttendCsv() {
        $db = Query::dbCheckOut();
        $item = explode(",", $this->selectdata_r);

        $query = knji100c_07Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_07Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }

        $yearAttend = array();
        $query = knji100c_07Query::selectAttendTitleQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $yearAttend[$row["YEAR"]] = "";
        }

        $contents = "";
        $subTitle = "";
        //ヘッダ作成
        if ($this->output == OUT_CODE_NAME) {
            $contents .= ",,,,,,";
            $subTitle .= "学籍番号,氏名,学年,組,出席番号,性別コード,性別";
        } else {
            $contents .= ",,,,,";
            $subTitle .= "学籍番号,氏名,学年,組,出席番号,性別";
        }
        foreach ($yearAttend as $year => $subclass) {
            $setYear = $year."年度";
            if ($this->selectdata_r == "SCHREG_ATTENDREC_DAT2") {
                $contents .= ",{$setYear}";
                $subTitle .= ",欠席日数";
            } else {
                $contents .= ",{$setYear},,,,,,";
                $subTitle .= ",授業日数,出停・忌引き等の日数,留学中の授業日数,出席しなければならない日数,欠席日数,出席日数,備考";
            }
        }
        if ($this->selectdata_r == "SCHREG_ATTENDREC_DAT2") {
            $contents .= ",";
            $subTitle .= ",欠席理由";
        }
        $contents .= "\n";
        $contents .= $subTitle;
        $contents .= "\n";
        $isKindai = $db->getOne(knji100c_07Query::isKindaiSql());
        $schArray = explode(",", $this->selectdata_l);
        foreach ($schArray as $key => $schregno) {
            $headFlg = true;
            $sep = "";
            if (get_count($yearAttend) > 0) {
                foreach ($yearAttend as $year => $subclass) {
                    $query = knji100c_07Query::selectAttendQuery($this, $year, $schregno, $isKindai, $headFlg);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                    if ($this->selectdata_r == "SCHREG_ATTENDREC_DAT2") {
                        unset($row['ATTEND_1']);
                        unset($row['SUSP_MOUR']);
                        unset($row['ABROAD']);
                        unset($row['REQUIREPRESENT']);
                        unset($row['PRESENT']);
                        unset($row['ATTENDREC_REMARK']);
                    }
                    $contents .= $sep.implode($row, ",");
                    $headFlg = false;
                    $sep = ",";
                }
            } else {
                $query = knji100c_07Query::selectAttendQuery($this, $year, $schregno, $isKindai, $headFlg);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                unset($row['ATTEND_1']);
                unset($row['SUSP_MOUR']);
                unset($row['ABROAD']);
                unset($row['REQUIREPRESENT']);
                unset($row['ATTEND_6']);
                unset($row['PRESENT']);
                unset($row['ATTENDREC_REMARK']);
                $contents .= $sep.implode($row, ",");
                $headFlg = false;
                $sep = ",";
            }
            if ($this->selectdata_r == "SCHREG_ATTENDREC_DAT2") {
                $query = knji100c_07Query::selectHexamRemarkQuery($schregno);
                $remark = $db->getOne($query);
                $contents .= $sep.$remark;
            }
            $contents .= "\n";
            $data = true;
        }

        Query::dbCheckIn($db);
        if ($data){
            $fileTitle = $this->selectdata_r == "SCHREG_ATTENDREC_DAT" ? "進学用" : "就職用";
            $filename = sprintf("%s.csv","出席データ({$fileTitle})");
            common::downloadContents($contents, $filename);
            exit;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CSV書出し
    function getHexamCsv() {
        $db = Query::dbCheckOut();
        $item = explode(",", $this->selectdata_r);

        $query = knji100c_07Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_07Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }

        $yearHtrain = array();
        if ($this->selectdata_r == "HTRAINREMARK_DAT") {
            $query = knji100c_07Query::selectHexamEntTitleQuery($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $yearHtrain[$row["YEAR"]] = "";
            }
        }

        $contents = "";
        $subTitle = "";
        //ヘッダ作成
        if ($this->output == OUT_CODE_NAME) {
            if ($this->selectdata_r == "HTRAINREMARK_DAT") {
                $contents .= ",,,,,,";
            }
            $subTitle .= "学籍番号,氏名,学年,組,出席番号,性別コード,性別";
        } else {
            if ($this->selectdata_r == "HTRAINREMARK_DAT") {
                $contents .= ",,,,,";
            }
            $subTitle .= "学籍番号,氏名,学年,組,出席番号,性別";
        }
        if ($this->selectdata_r == "HTRAINREMARK_DAT") {
            foreach ($yearHtrain as $year => $subclass) {
                $setYear = $year."年度";
                $contents .= ",{$setYear},,,";
                $subTitle .= ",特別活動の記録,学習における特徴等、行動の特徴・特技等,部活動、ボランティア活動等、取得資格、検定等,その他";
            }
            $contents .= ",総合的な学習,,";
            $subTitle .= ",内容,評価,備考";
        } else {
            $subTitle .= ",特別活動の記録,身体状況の備考,本人の長所・推薦事由";
        }
        if ($this->selectdata_r == "HTRAINREMARK_DAT") {
            $contents .= "\n";
        }
        $contents .= $subTitle;
        $contents .= "\n";
        $schArray = explode(",", $this->selectdata_l);
        foreach ($schArray as $key => $schregno) {
            $headFlg = true;
            $sep = "";
            if ($this->selectdata_r == "HTRAINREMARK_DAT") {
                if (get_count($yearHtrain) > 0) {
                    foreach ($yearHtrain as $year => $subclass) {
                        $query = knji100c_07Query::selectHexamEntQuery($this, $year, $schregno, $headFlg);
                        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                        $row = str_replace("\r\n", "↓", $row);
                        $row = str_replace("\r", "↓", $row);
                        $row = str_replace("\n", "↓", $row);
                        $contents .= $sep.implode($row, ",");
                        $headFlg = false;
                        $sep = ",";
                    }
                } else {
                    $query = knji100c_07Query::selectHexamEntQuery($this, $year, $schregno, $headFlg);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    unset($row['SPECIALACTREMARK']);
                    unset($row['TOTALREMARK']);
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                    $contents .= $sep.implode($row, ",");
                    $headFlg = false;
                    $sep = ",";
                }
                $query = knji100c_07Query::selectHexamEntHdatQuery($schregno);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace("\r\n", "↓", $row);
                $row = str_replace("\r", "↓", $row);
                $row = str_replace("\n", "↓", $row);
                if (is_array($row)) {
                    $contents .= $sep.implode($row, ",");
                }
            } else {
                $query = knji100c_07Query::selectHexamEmpQuery($this, $schregno, $headFlg);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace("\r\n", "↓", $row);
                $row = str_replace("\r", "↓", $row);
                $row = str_replace("\n", "↓", $row);
                $contents .= $sep.implode($row, ",");
                $headFlg = false;
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }

        Query::dbCheckIn($db);
        if ($data){
            $fileTitle = $this->selectdata_r == "HTRAINREMARK_DAT" ? "進学用" : "就職用";
            $filename = sprintf("%s.csv","所見データ({$fileTitle})");
            common::downloadContents($contents, $filename);
            exit;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>