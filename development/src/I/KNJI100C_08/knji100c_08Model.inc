<?php

require_once('for_php7.php');
class knji100c_08Model extends Model {

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
        $this->item[]["SCHREG_ATTENDREC_DAT"]   = "出席データ";
        $this->item[]["HTRAINREMARK_DAT"]       = "所見データ";

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
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '08';
        //対象生徒
        if(VARS::get("selectdata_l"))
            $this->selectdata_l     = VARS::get("selectdata_l");
        //学年
        if(VARS::get("GRADE"))
            $this->grade            = VARS::get("GRADE");
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
        } else if ($this->selectdata_r == "SCHREG_ATTENDREC_DAT") {
            return $this->getAttendCsv();
        } else {
            return $this->getHtrainCsv();
        }

    }

    //CSV書出し
    function getStudyRecCsv() {
        $db = Query::dbCheckOut();
        $item = explode(",", $this->selectdata_r);

        $query = knji100c_08Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_08Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }

        $yearSubclass = array();
        $query = knji100c_08Query::selectSubclassTitleQuery($this);
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
                $contents .= ",{$setYear},,,,";
                $subTitle .= ",{$subCd},成績,履修単位,修得単位,備考";
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
                        $query = knji100c_08Query::selectStudyRecQuery($this, $year, $subCd, $schregno, $headFlg);
                        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                        if (!empty($row)) {
                            $contents .= $sep.implode($row, ",");
                        }
                        $headFlg = false;
                        $sep = ",";
                    }
                }
            } else {
                $query = knji100c_08Query::selectStudyRecQuery($this, $year, $subCd, $schregno, $headFlg);
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

        $query = knji100c_08Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_08Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }

        $yearAttend = array();
        $query = knji100c_08Query::selectAttendTitleQuery($this);
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
            $contents .= ",{$setYear},,,,,,";
            $subTitle .= ",授業日数,出停・忌引き等の日数,留学中の授業日数,出席しなければならない日数,欠席日数,出席日数,備考";
        }
        $contents .= "\n";
        $contents .= $subTitle;
        $contents .= "\n";
        $isKindai = $db->getOne(knji100c_08Query::isKindaiSql());
        $schArray = explode(",", $this->selectdata_l);
        foreach ($schArray as $key => $schregno) {
            $headFlg = true;
            $sep = "";
            if (get_count($yearAttend) > 0) {
                foreach ($yearAttend as $year => $subclass) {
                    $query = knji100c_08Query::selectAttendQuery($this, $year, $schregno, $isKindai, $headFlg);
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
                $query = knji100c_08Query::selectAttendQuery($this, $year, $schregno, $isKindai, $headFlg);
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
            $contents .= "\n";
            $data = true;
        }

        Query::dbCheckIn($db);
        if ($data){
            $filename = sprintf("%s.csv","指導要録(出席データ)");
            common::downloadContents($contents, $filename);
            exit;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CSV書出し
    function getHtrainCsv() {
        $db = Query::dbCheckOut();
        $item = explode(",", $this->selectdata_r);

        $query = knji100c_08Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_08Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }

        $yearHtrain = array();
        $query = knji100c_08Query::selectHtrainTitleQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $yearHtrain[$row["YEAR"]] = "";
        }

        $contents = "";
        $subTitle = "";
        //ヘッダ作成
        if ($this->output == OUT_CODE_NAME) {
            $contents .= ",,,,,,,,";
            $subTitle .= "学籍番号,氏名,学年,組,出席番号,性別コード,性別,学習活動,評価";
        } else {
            $contents .= ",,,,,,,";
            $subTitle .= "学籍番号,氏名,学年,組,出席番号,性別,学習活動,評価";
        }
        foreach ($yearHtrain as $year => $subclass) {
            $setYear = $year."年度";
            $contents .= ",{$setYear},";
            $subTitle .= ",特別活動の記録,総合所見及び指導上参考となる諸事項";
        }
        $contents .= "\n";
        $contents .= $subTitle;
        $contents .= "\n";
        $isKindai = $db->getOne(knji100c_08Query::isKindaiSql());
        $schArray = explode(",", $this->selectdata_l);
        foreach ($schArray as $key => $schregno) {
            $headFlg = true;
            $sep = "";
            if (get_count($yearHtrain) > 0) {
                foreach ($yearHtrain as $year => $subclass) {
                    $query = knji100c_08Query::selectHtrainQuery($this, $year, $schregno, $headFlg);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($headFlg && $this->Properties["seitoSidoYorokuTotalStudyCombineHtrainremarkDat"] == "1") {
                        $row["TOTALSTUDYACT"] = "";
                        $row["TOTALSTUDYVAL"] = "";
                        $query = knji100c_08Query::selectHtrainTotalQuery($schregno);
                        $result = $db->query($query);
                        $totalact = "";
                        $totalval = "";
                        while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            if ($totalact != '' && $row1["TOTALSTUDYACT"] != '') { $totalact .= "\n";}
                            $totalact .= $row1["TOTALSTUDYACT"];
                            if ($totalval != '' && $row1["TOTALSTUDYVAL"] != '') { $totalval .= "\n";}
                            $totalval .= $row1["TOTALSTUDYVAL"];
                        }
                        $row["TOTALSTUDYACT"] = $totalact;
                        $row["TOTALSTUDYVAL"] = $totalval;
                    }
                    $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                    $contents .= $sep.implode($row, ",");
                    $headFlg = false;
                    $sep = ",";
                }
            } else {
                $query = knji100c_08Query::selectHtrainQuery($this, $year, $schregno, $headFlg);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($this->Properties["seitoSidoYorokuTotalStudyCombineHtrainremarkDat"] == "1") {
                    $row["TOTALSTUDYACT"] = "";
                    $row["TOTALSTUDYVAL"] = "";
                }
                $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                unset($row['SPECIALACTREMARK']);
                unset($row['TOTALREMARK']);
                $contents .= $sep.implode($row, ",");
                $headFlg = false;
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }

        Query::dbCheckIn($db);
        if ($data){
            $filename = sprintf("%s.csv","指導要録(所見データ)");
            common::downloadContents($contents, $filename);
            exit;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
