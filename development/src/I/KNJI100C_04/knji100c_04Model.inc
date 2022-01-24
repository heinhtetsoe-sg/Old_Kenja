<?php

require_once('for_php7.php');
class knji100c_04Model extends Model
{
    public $cmd;
    public $schregno;                  //学籍番号
    public $control_data = array();    //コントロールマスタデータを保持
    public $item;
    public $schItemList;
    public $selectdata_l;              //対象生徒
    public $selectdata_r;              //書出し項目一覧
    public $koteidata;                 //CSV項目(固定)
    public $kotei;
    public $recordTable;
    public $testTable;
    public $testKind;

    public function knja150Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->schItemList = array("SCHREGNO", "NAME", "GRADE", "HR_CLASS", "ATTENDNO", "SEX");

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
        //出力設定
        $this->output           = VARS::post("OUTPUT");
        //テスト種別
        $this->testKind         = VARS::post("TESTKIND");
        //プロパティファイル
        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
        //初期値の設定
        $this->recordTable = (strlen($this->Properties["useRecordDat"])) ? $this->Properties["useRecordDat"] : "KIN_RECORD_DAT";
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG";
        /**********************************/
        /************ 選択項目 ************/
        /**********************************/
        
        $this->kotei = array();
        $this->kotei[]["SCHREGNO"]                    = "学籍番号";
        $this->kotei[]["NAME"]                        = "生徒氏名";
        $this->kotei[]["GRADE"]                       = "学年";
        $this->kotei[]["HR_CLASS"]                    = "組";
        $this->kotei[]["SEX"]                         = "性別区分(*)";

        $this->item = array();
        if ($this->recordTable == "KIN_RECORD_DAT") {
            $this->item = $this->getItemKinRecordDat();
        } else {
            $this->item = $this->getItemRecordScoreDat();
        }
        /**********************************/
        /************ パラメータ **********/
        /**********************************/
        //サブシステム
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '04';
        //対象生徒
        if (VARS::get("selectdata_l")) {
            $this->selectdata_l     = VARS::get("selectdata_l");
        }
        //学年
        if (VARS::get("GRADE")) {
            $this->grade            = VARS::get("GRADE");
        }
        //年組
        if (VARS::get("GRADE_HR_CLASS")) {
            $this->grade_hr_class   = VARS::get("GRADE_HR_CLASS");
        }
        //出力設定
        if (VARS::get("OUTPUT")) {
            $this->output           = VARS::get("OUTPUT");
        }

        //校種取得
        $this->schoolkind = "H";
        if ($this->grade_hr_class) {
            $db = Query::dbCheckOut();
            $this->schoolkind = $db->getOne(knji100c_04Query::getSchoolKind($this));
            Query::dbCheckIn($db);
        }
    }

    /**************************************************/
    /************ 選択項目(KIN_RECORD_DAT) ************/
    /**************************************************/
    public function getItemKinRecordDat()
    {
        $item = array();

        $item[]["ATTENDNO"]                     = "出席番号";
        $item[]["SUBCLASSCD"]                   = "科目(*)";
        $item[]["CHAIRCD"]                      = "講座(*)";
        if ($this->testKind == "") {
            $item[]["SEM1_INTER_REC"]           = "１学期中間成績";
            $item[]["SEM1_TERM_REC"]            = "１学期期末成績";
            $item[]["SEM1_REC"]                 = "１学期成績";
            $item[]["SEM2_INTER_REC"]           = "２学期中間成績";
            $item[]["SEM2_TERM_REC"]            = "２学期期末成績";
            $item[]["SEM2_REC"]                 = "２学期成績";
            $item[]["SEM3_TERM_REC"]            = "３学期期末成績";
            $item[]["SEM3_REC"]                 = "３学期成績";
            $item[]["SEM1_INTER_REC_FLG"]       = "１学期中間成績_フラグ";
            $item[]["SEM1_TERM_REC_FLG"]        = "１学期期末成績_フラグ";
            $item[]["SEM1_REC_FLG"]             = "１学期成績_フラグ";
            $item[]["SEM2_INTER_REC_FLG"]       = "２学期中間成績_フラグ";
            $item[]["SEM2_TERM_REC_FLG"]        = "２学期期末成績_フラグ";
            $item[]["SEM2_REC_FLG"]             = "２学期成績_フラグ";
            $item[]["SEM3_TERM_REC_FLG"]        = "３学期期末成績_フラグ";
            $item[]["SEM3_REC_FLG"]             = "３学期成績_フラグ";
            $item[]["SEM1_INTER_REC_DI"]        = "１学期中間成績_出欠情報";
            $item[]["SEM1_TERM_REC_DI"]         = "１学期期末成績_出欠情報";
            $item[]["SEM2_INTER_REC_DI"]        = "２学期中間成績_出欠情報";
            $item[]["SEM2_TERM_REC_DI"]         = "２学期期末成績_出欠情報";
            $item[]["SEM3_TERM_REC_DI"]         = "３学期期末成績_出欠情報";
            $item[]["GRADE_RECORD"]             = "学年成績";
            $item[]["A_PATTERN_ASSESS"]         = "類型Ａ_評定";
            $item[]["B_PATTERN_ASSESS"]         = "類型Ｂ_評定";
            $item[]["C_PATTERN_ASSESS"]         = "類型Ｃ_評定";
            $item[]["JUDGE_PATTERN"]            = "判定類型";
            $item[]["GRADE_ASSESS"]             = "学年評定";
            $item[]["GRADE3_RELAASSESS_5STEP"]  = "３年時相対評価_５段階";
            $item[]["GRADE3_RELAASSESS_10STEP"] = "３年時相対評価_１０段階";
        } elseif ($this->testKind == "10101") {
            $item[]["SEM1_INTER_REC"]           = "１学期中間成績";
            $item[]["SEM1_INTER_REC_FLG"]       = "１学期中間成績_フラグ";
            $item[]["SEM1_INTER_REC_DI"]        = "１学期中間成績_出欠情報";
        } elseif ($this->testKind == "10201") {
            $item[]["SEM1_TERM_REC"]            = "１学期期末成績";
            $item[]["SEM1_TERM_REC_FLG"]        = "１学期期末成績_フラグ";
            $item[]["SEM1_TERM_REC_DI"]         = "１学期期末成績_出欠情報";
        } elseif ($this->testKind == "19900") {
            $item[]["SEM1_REC"]                 = "１学期成績";
            $item[]["SEM1_REC_FLG"]             = "１学期成績_フラグ";
        } elseif ($this->testKind == "20101") {
            $item[]["SEM2_INTER_REC"]           = "２学期中間成績";
            $item[]["SEM2_INTER_REC_FLG"]       = "２学期中間成績_フラグ";
            $item[]["SEM2_INTER_REC_DI"]        = "２学期中間成績_出欠情報";
        } elseif ($this->testKind == "20201") {
            $item[]["SEM2_TERM_REC"]            = "２学期期末成績";
            $item[]["SEM2_TERM_REC_FLG"]        = "２学期期末成績_フラグ";
            $item[]["SEM2_TERM_REC_DI"]         = "２学期期末成績_出欠情報";
        } elseif ($this->testKind == "29900") {
            $item[]["SEM2_REC"]                 = "２学期成績";
            $item[]["SEM2_REC_FLG"]             = "２学期成績_フラグ";
        } elseif ($this->testKind == "30201") {
            $item[]["SEM3_TERM_REC"]            = "３学期期末成績";
            $item[]["SEM3_TERM_REC_FLG"]        = "３学期期末成績_フラグ";
            $item[]["SEM3_TERM_REC_DI"]         = "３学期期末成績_出欠情報";
        } elseif ($this->testKind == "39900") {
            $item[]["SEM3_REC"]                 = "３学期成績";
            $item[]["SEM3_REC_FLG"]             = "３学期成績_フラグ";
        } elseif ($this->testKind == "99900") {
            $item[]["GRADE_RECORD"]             = "学年成績";
            $item[]["A_PATTERN_ASSESS"]         = "類型Ａ_評定";
            $item[]["B_PATTERN_ASSESS"]         = "類型Ｂ_評定";
            $item[]["C_PATTERN_ASSESS"]         = "類型Ｃ_評定";
            $item[]["JUDGE_PATTERN"]            = "判定類型";
            $item[]["GRADE_ASSESS"]             = "学年評定";
            $item[]["GRADE3_RELAASSESS_5STEP"]  = "３年時相対評価_５段階";
            $item[]["GRADE3_RELAASSESS_10STEP"] = "３年時相対評価_１０段階";
        }

        return $item;
    }

    /**********************************/
    /**  選択項目(RECORD_SCORE_DAT)  **/
    /**********************************/
    public function getItemRecordScoreDat()
    {
        $item = array();

        $item[]["ATTENDNO"]         = "出席番号";

        $db = Query::dbCheckOut();
        if ($this->testTable == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query = knji100c_04Query::getTestKindSdiv($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $item[]["SCORE_".$row["VALUE"]]  = $row["ITEMNAME"];
            }
        } else {
            $query = knji100c_04Query::getTestKind($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $score_div = ($row["TESTKINDCD"] == "99") ? "00" : "01";
                $item[]["SCORE_".$row["VALUE"].$score_div]  = $row["ITEMNAME"];
            }
        }
        Query::dbCheckIn($db);

        return $item;
    }

    //CSV書出し
    public function &getCsvModel()
    {
        $db           = Query::dbCheckOut();
        $this->koteidata = "";
        foreach ($this->kotei as $key => $val) {
            foreach ($val as $field => $label) {
                $this->koteidata = $this->koteidata.$field.",";
            }
        }
        $item = explode(",", $this->selectdata_r);

        $query = knji100c_04Query::getDelFieldSql($this);
        $db->query($query);
        $setSeq = 1;
        foreach ($item as $key => $val) {
            $query = knji100c_04Query::getFieldInsSql($this, $setSeq, $val);
            $db->query($query);
            $setSeq++;
        }
        $this->selectdata_r = $this->koteidata.$this->selectdata_r;
        $item = explode(",", $this->selectdata_r);


        //項目名一覧
        $item_array = array();
        foreach ($this->kotei as $key => $val) {
            foreach ($val as $field => $label) {
                $item_array[$field] = $label;
            }
        }
        foreach ($this->item as $key => $val) {
            foreach ($val as $field => $label) {
                $item_array[$field] = $label;
            }
        }

        //item
        $schregItem = array();
        $recordItem = array();
        for ($i = 0; $i < get_count($item); $i++) {
            $codePrefix = str_replace("(*)", "", $item_array[$item[$i]]);
            if ($this->recordTable == "KIN_RECORD_DAT") {
                $code = ($this->output == OUT_CODE_NAME && in_array($item[$i], array("SEX", "SUBCLASSCD", "CHAIRCD"))) ? $codePrefix."コード," : "";
            } else {
                $code = ($this->output == OUT_CODE_NAME && in_array($item[$i], array("SEX"))) ? $codePrefix."コード," : "";
            }
            if (in_array($item[$i], $this->schItemList)) {
                $schregItem[$item[$i]] = $code.$item_array[$item[$i]];
            } else {
                $recordItem[$item[$i]] = $code.$item_array[$item[$i]];
            }
        }

        //data
        $dataTemp = array();
        $tempCnt = array();
        if ($this->recordTable == "KIN_RECORD_DAT") {
            $query  = knji100c_04Query::selectQuery($this);
            $result  = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($dataTemp[$row["SCHREGNO"]] == "") {
                    foreach ($schregItem as $key => $val) {
                        $dataTemp[$row["SCHREGNO"]][] = $row[$key];
                    }
                    foreach ($recordItem as $key => $val) {
                        $dataTemp[$row["SCHREGNO"]][] = $row[$key];
                    }
                } else {
                    foreach ($recordItem as $key => $val) {
                        $dataTemp[$row["SCHREGNO"]][] = $row[$key];
                    }
                }
                $tempCnt[$row["SCHREGNO"]]++;
            }
        } else {
            $query = knji100c_04Query::selectQueryRecordScore($this, $recordItem);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($dataTemp[$row["SCHREGNO"]] == "") {
                    foreach ($schregItem as $key => $val) {
                        $dataTemp[$row["SCHREGNO"]][] = $row[$key];
                    }
                    foreach ($recordItem as $key => $val) {
                        list($item, $testcd) = explode('_', $key);
                        $dataTemp[$row["SCHREGNO"]][] = $row["CHAIRCD_".$testcd];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSCD_".$testcd];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SCORE_".$testcd];
                        if (substr($testcd, 0, 1) != "9" && substr($testcd, 1, 2) != "99") {
                            $dataTemp[$row["SCHREGNO"]][] = $row["DI_".$testcd];
                        }
                    }
                } else {
                    foreach ($recordItem as $key => $val) {
                        list($item, $testcd) = explode('_', $key);
                        $dataTemp[$row["SCHREGNO"]][] = $row["CHAIRCD_".$testcd];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSCD_".$testcd];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SCORE_".$testcd];
                        if (substr($testcd, 0, 1) != "9" && substr($testcd, 1, 2) != "99") {
                            $dataTemp[$row["SCHREGNO"]][] = $row["DI_".$testcd];
                        }
                    }
                }
                $tempCnt[$row["SCHREGNO"]]++;
            }
        }
        //ヘッダ作成
        $contents = $sep = "";
        foreach ($schregItem as $key => $val) {
            $contents .= $sep .$val;
            $sep = ",";
        }
        $headCnt = 0;
        foreach ($tempCnt as $key => $val) {
            $headCnt = $headCnt < $val ? $val : $headCnt;
        }
        if ($this->output == OUT_CODE_NAME) {
            $chairCode = "講座コード,";
            $subclassCode = "科目コード,";
        }
        for ($i = 0; $i < $headCnt; $i++) {
            foreach ($recordItem as $key => $val) {
                list($item, $testcd) = explode('_', $key);
                $contents .= $sep .$chairCode."講座(*)";
                $contents .= $sep .$subclassCode."科目(*)";
                $contents .= $sep .$val;
                if (substr($testcd, 0, 1) != "9" && substr($testcd, 1, 2) != "99") {
                    $contents .= $sep .$val."-欠席";
                }
                if ($sep == "") {
                    $sep = ",";
                }
            }
        }
        $contents .= "\n";
        //データ部作成
        $data = false;
        foreach ($dataTemp as $key => $val) {
            $contents .= implode($val, ",") ."\n";
            $data = true;
        }
        Query::dbCheckIn($db);
        if ($data) {
            $filename = sprintf("%s.csv", "生徒基礎データ（成績情報）");
            common::downloadContents($contents, $filename);
            exit;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
