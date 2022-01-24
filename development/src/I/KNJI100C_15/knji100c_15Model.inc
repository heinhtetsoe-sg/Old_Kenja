<?php

require_once('for_php7.php');

class knji100c_15Model extends Model {

    var $cmd;
    var $schregno;                  //学籍番号
    var $control_data = array();    //コントロールマスタデータを保持
    var $item;
    var $selectdata_l;              //対象生徒
    var $selectdata_r;              //書出し項目一覧
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
        //学年
        $this->grade            = VARS::post("GRADE");
        //年組
        $this->grade_hr_class   = VARS::post("GRADE_HR_CLASS");
        //異動対象日付
        $this->date             = VARS::post("DATE");
        //出力設定
        $this->output           = VARS::post("OUTPUT");
        //業者コード
        $this->companycd        = VARS::post("COMPANYCD");

        //サブシステム
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '15';
        //対象生徒
        if(VARS::get("selectdata_l"))
            $this->selectdata_l     = VARS::get("selectdata_l");
        //年組
        if(VARS::get("GRADE_HR_CLASS"))
            $this->grade_hr_class   = VARS::get("GRADE_HR_CLASS");
        //出力設定
        if(VARS::get("OUTPUT"))
            $this->output = VARS::get("OUTPUT");
    }

    //CSV書出し
    function &getCsvModel() {
        $db = Query::dbCheckOut();

        $contents = "";
        //ヘッダ作成
        if ($this->output == OUT_CODE_NAME) {
            $contents .= "学籍番号,氏名,学年,組,出席番号,性別コード,性別,教材名";
        } else {
            $contents .= "学籍番号,氏名,学年,組,出席番号,性別,教材名";
        }

        $mockSubclassCdList = array();
        $sep = ",";
        $query = knji100c_15Query::getHeaderSubclassName($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= $sep.$row["SUBCLASS_NAME"]."－得点率";
            $contents .= $sep.$row["SUBCLASS_NAME"]."－GTZ";
            $contents .= $sep.$row["SUBCLASS_NAME"]."－偏差値(全国)";
            $contents .= $sep.$row["SUBCLASS_NAME"]."－偏差値(校内)";
            $contents .= $sep.$row["SUBCLASS_NAME"]."－順位(全国)";
            $contents .= $sep.$row["SUBCLASS_NAME"]."－順位(校内)";

            $mockSubclassCdList[] = $row["MOCK_SUBCLASS_CD"];
        }
        $result->free();

        $contents .= "\n";

        $mockCdList = explode(",", $this->selectdata_r);
        foreach ($mockCdList as $mockCd) {
            $schList = explode(",", $this->selectdata_l);
            foreach ($schList as $schregno) {
                $query = knji100c_15Query::getStudentInfo($schregno);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $query2 = knji100c_15Query::selectDataQuery($mockCd, $schregno);
                    $result2 = $db->query($query2);
                    $score = array();
                    while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $mockname  = $row2["MOCKNAME1"];
                        $score[$row2["MOCK_SUBCLASS_CD"]] = $row2;
                    }
                    if ($score) {
                        $data = true;
                        $contents .= $row["SCHREGNO"];
                        $contents .= $sep.$row["NAME_SHOW"];
                        $contents .= $sep.$row["GRADE"];
                        $contents .= $sep.$row["HR_CLASS"];
                        $contents .= $sep.$row["ATTENDNO"];
                        if ($this->output == OUT_CODE_NAME) {
                            $contents .= $sep.$row["SEX"];
                            $contents .= $sep.$row["SEX_NAME"];
                        } else if ($this->output == OUT_CODE_ONLY) {
                            $contents .= $sep.$row["SEX"];
                        } else {
                            $contents .= $sep.$row["SEX_NAME"];
                        }
                        $contents .= $sep.$mockname;
                        foreach ($mockSubclassCdList as $mockSubclassCd) {
                            $contents .= $sep.$score[$mockSubclassCd]["SCORE"];
                            $contents .= $sep.$score[$mockSubclassCd]["GTZ"];
                            $contents .= $sep.$score[$mockSubclassCd]["ALL_DEVIATION"];
                            $contents .= $sep.$score[$mockSubclassCd]["SCHOOL_DEVIATION"];
                            $contents .= $sep.$score[$mockSubclassCd]["ALL_RANK"];
                            $contents .= $sep.$score[$mockSubclassCd]["SCHOOL_RANK"];
                        }
                        $contents .= "\n";
                    }
                }
            }
        }

        Query::dbCheckIn($db);

        if ($data){
            $filename = sprintf("%s.csv","外部模試情報");
            common::downloadContents($contents, $filename);
            exit;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>