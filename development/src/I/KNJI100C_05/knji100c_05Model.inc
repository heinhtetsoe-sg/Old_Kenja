<?php

require_once('for_php7.php');
class knji100c_05Model extends Model {

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
        //模試区分
        $this->mockSyubetu      = VARS::post("MOCK_SYUBETU");
        //順位の基準
        $this->mockDiv          = VARS::post("MOCK_DIV");

        //サブシステム
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '05';

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

        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
    }

    //CSV書出し
    function &getCsvModel() {
        $db = Query::dbCheckOut();
        $item = explode(",", $this->selectdata_r);

        $query = knji100c_05Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_05Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }

        $yearSubclass = array();
        $query = knji100c_05Query::selectSubclassTitleQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $yearSubclass[$row["YEAR"]][$row["MOCKCD"]][] = $row;
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
        foreach ($yearSubclass as $year => $mockData) {
            $firstFlg = true;
            foreach ($mockData as $mockCd => $subclassArray) {
                $setYear = $firstFlg ? $year."年度" : "";
                $contents .= ",{$setYear}";
                $firstFlg = true;
                foreach ($subclassArray as $key => $val) {
                    $contents .= $firstFlg ? "{$val["MOCKNAME1"]},,,," : ",,,,,";
                    $subTitle .= ",{$val["SUBCLASS_NAME"]},成績,学年順位,クラス順位,コース順位";
                    $firstFlg = false;
                }
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
                foreach ($yearSubclass as $year => $mockData) {
                    foreach ($mockData as $mockCd => $subclassArray) {
                        foreach ($subclassArray as $key => $val) {
                            $query = knji100c_05Query::selectMockRankQuery($this, $year, $val["MOCKCD"], $val["MOCK_SUBCLASS_CD"], $schregno, $headFlg);
                            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                            $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                            $contents .= $sep.implode($row, ",");
                            $headFlg = false;
                            $sep = ",";
                        }
                    }
                }
            } else {
                $query = knji100c_05Query::selectMockRankQuery($this, $year, "", $subCd, $schregno, $headFlg);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                unset($row['MOCK_SUBCLASS_CD']);
                unset($row['SCORE']);
                unset($row['GRADE_RANK']);
                unset($row['CLASS_RANK']);
                unset($row['COURSE_RANK']);
                $contents .= $sep.implode($row, ",");
                $headFlg = false;
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }

        Query::dbCheckIn($db);
        if ($data){
            $filename = sprintf("%s.csv","実力情報");
            common::downloadContents($contents, $filename);
            exit;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }
}
?>