<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd126bModel.inc 56581 2017-10-22 12:37:16Z maeshiro $

class knjd126bModel extends Model
{
    public $field = array();
    public $data  = array();
    public $creditflg;
    public $cmd;
    public $testcd;
    public $jitugi_flg;
    public $value_cal_flg;
    public $usePerfect;
    public $valueSmesterFlg = array();
    public $isJitu;
    public $absenceDiv;
    public $lastColumn;//CSVの最後の列の値

    public function knjd126bModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        $this->valueSmesterFlg = array();
        $this->cmd     = VARS::request("cmd");
        $this->field["SUBCLASSCD"] = strlen(VARS::post("SUBCLASSCD")) ? VARS::post("SUBCLASSCD") : VARS::post("HID_SUBCLASSCD");
        if ($this->cmd == "update") {
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
        }

        $postChaircd = strlen(VARS::post("CHAIRCD")) ? VARS::post("CHAIRCD") : VARS::post("HID_CHAIRCD");
        if ($this->cmd != "subclasscd" && $postChaircd) {
            $this->field["CHAIRCD"] = $postChaircd;
        } elseif ($this->cmd == "update") {
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }

        //プロパティファイル
        $this->getPropertiesAll();

        for ($i = 1; $i <= 5; $i++) {
            $this->field["CHK_COMP".$i] = VARS::post("CHK_COMP".$i);
        }
        for ($i=1; $i<=4; $i++) {
            $this->field["CHK_COMP_VALUE".$i] = VARS::post("CHK_COMP_VALUE".$i);
        }
        $this->field["CHK_FLG"]  = VARS::post("CHK_FLG");
        $this->field["COUNT"]  = VARS::post("COUNT"); //行数
        $this->value_cal_flg  = VARS::post("VALUE_CAL_FLG");
        $this->getProperties();
        // V_SCHOOL_MSTから授業数換算フラグを取得
        $rtnRow = knjd126bQuery::getSchoolMst();
        $this->isJitu = $rtnRow["JUGYOU_JISU_FLG"] == "2" ? true : false; // 1:法定授業、2:実授業
        $this->absenceDiv = "1"; // 1:年間、2:随時

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function getProperties()
    {
        $posFlg2 = false;
        $retVal = "";
        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/prgInfo.properties")) {
            $filename = CONFDIR ."/prgInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/prgInfo.properties";
        }

        $fp = @fopen($filename, 'r');
        while ($line = fgets($fp, 1024)) {
            $pos = strpos($line, "usePerfect");
            // === を使用していることに注目しましょう。単純に == を使ったのでは
            // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
            if ($pos === false) {
                if (!$posFlg2) {
                    $this->usePerfect = false;
                }
            } else {
                $retVal = str_replace("usePerfect = ", "", $line);
                $retVal = str_replace("\r\n", "", $retVal);
                $retVal = str_replace("\r", "", $retVal);
                $retVal = str_replace("\n", "", $retVal);
                $this->usePerfect = $retVal;
                $posFlg2 = true;
            }
        }
        fclose($fp);

        return $retVal;
    }

    public function validateInput()
    {
        return true;
    }

    public function &getUpdateModel()
    {
        $term = array("SCORE1111","SCORE1211","SCORE1901","VALUE1900","SCORE2111","SCORE2211","SCORE2901","VALUE2900","SCORE3211","SCORE3901","VALUE3900","PROV_FLG","GRAD_VALUE","COMP_CREDIT","GET_CREDIT");

        $this->fields = array();
        $this->isScoreAllNull = array();
        for ($j = 0; $j < get_count($term); $j++) {
            $this->isScoreAllNull[$term[$j]] = false;
            for ($i = 0; $i < $this->field["COUNT"]; $i++) {
                $term_name = $term[$j]."-".$i;
                $post_term = VARS::post($term_name);
                if (isset($post_term)) {
                    $this->fields[$term[$j]][]   = $post_term;
                    if (strlen($post_term)) {
                        $this->isScoreAllNull[$term[$j]] = true;
                    }
                } else {
                    $this->fields[$term[$j]][]   = VARS::post($term[$j]."_DATA"."-".$i);
                    if (strlen(VARS::post($term[$j]."_DATA"."-".$i))) {
                        $this->isScoreAllNull[$term[$j]] = true;
                    }
                }
                $this->fields[$term[$j]."_ATTEND"][] = VARS::post($term[$j]."_ATTEND"."-".$i);
            }
        }
        for ($i = 0; $i < $this->field["COUNT"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            $this->fields["GRADE"][] = VARS::post("GRADE"."-".$i);
            $this->fields["NOTICE_LATE"][] = VARS::post("NOTICE_LATE"."-".$i);
            $this->fields["COMBINED_CREDIT_SUM"][] = VARS::post("COMBINED_CREDIT_SUM"."-".$i);
            $this->fields["ADJUST1"][] = VARS::post("ADJUST1"."-".$i);
            $this->fields["ADJUST2"][] = VARS::post("ADJUST2"."-".$i);
            $this->fields["ADJUST3"][] = VARS::post("ADJUST3"."-".$i);
        }

        knjd126bQuery::updateRecord($this);
        knjd126bQuery::updateExecuted($this);
        knjd126bQuery::updateExecuted2($this);
        $this->setMessage("MSG201");
    }
}
//var_dump($_REQUEST);
