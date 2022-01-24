<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjm550mModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;
    public $select_data = array();
    public $selectleft;
    public $searchField = array();
    public $accIndex;
    public $panelCnt;
    public $mainSearch;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjm550mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("KIJUN"            =>  VARS::post("KIJUN"),            //提出基準日
                             "GRADVAL_PRINT"    =>  VARS::post("GRADVAL_PRINT"),    //評定印字
                             "COMMENT1"         =>  VARS::post("COMMENT1"),         //コメント1
                             "COMMENT2"         =>  VARS::post("COMMENT2"),         //コメント2
                             "COMMENT3"         =>  VARS::post("COMMENT3"),         //コメント3
                             "COMMENT4"         =>  VARS::post("COMMENT4"),         //コメント4
                             "COMMENT5"         =>  VARS::post("COMMENT5"),         //コメント5
                             "COMMENT6"         =>  VARS::post("COMMENT6"),         //コメント6
                             "DBNAME"           =>  VARS::post("DBNAME"));          //DB変更に備えて

        $this->selectleft = VARS::post("selectleft");
        $this->type_div = VARS::post("TYPE_DIV");
        $this->mainSearch = VARS::post("MAIN_SEARCH");
        $this->cmd = VARS::request("cmd");

        //パネル系
        $this->accIndex = VARS::post("ACCINDEX");
        $this->panelCnt = 3;

        for ($i = 1; $i <= $this->panelCnt; $i++) {
            $this->searchField["SUBCLASS".$i] = VARS::post("SUBCLASS".$i);
            $this->searchField["SEARCH_S".$i] = VARS::post("SEARCH_S".$i);
            $this->searchField["REPORT".$i] = VARS::post("REPORT".$i);
            $this->searchField["REPORT_S".$i] = VARS::post("REPORT_S".$i);
            $this->searchField["SCHOOLING".$i] = VARS::post("SCHOOLING".$i);
            $this->searchField["SCHOOLING_S".$i] = VARS::post("SCHOOLING_S".$i);
            $this->searchField["SCORE".$i] = VARS::post("SCORE".$i);
            $this->searchField["SCORE_S".$i] = VARS::post("SCORE_S".$i);
            $this->searchField["HYOUTEI".$i] = VARS::post("HYOUTEI".$i);
            $this->searchField["HYOUTEI_S".$i] = VARS::post("HYOUTEI_S".$i);
        }

        if ($this->cmd == "") {
            unset($this->searchField);
            unset($this->accIndex);
        }
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
    
    //入力項目チェック
    public function validateInput()
    {
        if (strlen($this->field["COMMENT1"]) > 210) {
            $this->setWarning("MSG915", strlen($this->field["COMMENT1"])."(備考は210バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMMENT2"]) > 210) {
            $this->setWarning("MSG915", strlen($this->field["COMMENT2"])."(備考は210バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMMENT3"]) > 210) {
            $this->setWarning("MSG915", strlen($this->field["COMMENT3"])."(備考は210バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMMENT4"]) > 210) {
            $this->setWarning("MSG915", strlen($this->field["COMMENT4"])."(備考は210バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMMENT5"]) > 210) {
            $this->setWarning("MSG915", strlen($this->field["COMMENT5"])."(備考は210バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMMENT6"]) > 210) {
            $this->setWarning("MSG915", strlen($this->field["COMMENT6"])."(備考は210バイトまでです)");
            return false;
        }
        return true;
    }
    
    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $tmpdata = array();
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
            for ($upcnt = 1; $upcnt <= 6; $upcnt++) {
                $this->update($upcnt);
            }
        }
    }
    public function &delete()
    {
        $result = knjm550mQuery::getDeleteQuery($this);

        return $result;
    }

    public function &update($cnt)
    {
        $result = knjm550mQuery::getUpdateQuery($this, $cnt);

        return $result;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $header = array("YEAR"            => "年度",
                        "SEMESTER"        => "学期",
                        "SCHREGNO"        => "学籍番号",
                        "GRADE"           => "学年",
                        "HR_CLASS"        => "クラス",
                        "ATTENDNO"        => "出席番号",
                        "NAME"            => "氏名",
                        "NAME_KANA"       => "かな氏名",
                        $this->lastColumn => $this->lastColumn);

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        $setMainSearch = $this->mainSearch == "1" ? "AND条件" : "OR条件";
        $contents .= $setMainSearch."\n";

        for ($i = 1; $i <= $this->panelCnt; $i++) {
            if ($this->searchField["SUBCLASS".$i]) {
                list($chairCd, $subclassCd) = preg_split("/-/", $this->searchField["SUBCLASS".$i]);
                $setSubclassName = $db->getOne(knjm550mQuery::getSubclassName($this, $subclassCd));
                $setSubSearch = $this->searchField["SEARCH_S".$i] == "1" ? "、AND条件" : "、OR条件";

                $setReportSearch  = "";
                $setSchoolingSearch = "";
                $setScoreSearch = "";
                $setValueSearch = "";
                if ($this->searchField["REPORT".$i]) {
                    $setReportSearch  = "、レポート提出";
                    $setReportSearch .= $this->searchField["REPORT".$i]."回";
                    if ($this->searchField["REPORT_S".$i] == "1") {
                        $setReportSearch .= "まで提出済み";
                    } elseif ($this->searchField["REPORT_S".$i] == "2") {
                        $setReportSearch .= "まで合格";
                    } else {
                        $setReportSearch .= "までに不合格・未提出あり";
                    }
                }

                if ($this->searchField["SCHOOLING".$i]) {
                    $setSchoolingSearch  = "、スクーリング出席回数";
                    $setSchoolingSearch .= $this->searchField["SCHOOLING".$i]."回";
                    if ($this->searchField["SCHOOLING_S".$i] == "1") {
                        $setSchoolingSearch .= "に等しい";
                    } elseif ($this->searchField["SCHOOLING_S".$i] == "2") {
                        $setSchoolingSearch .= "以上";
                    } else {
                        $setSchoolingSearch .= "以下";
                    }
                }

                if ($this->searchField["SCORE".$i]) {
                    $setScoreSearch  = "、試験得点";
                    $setScoreSearch .= $this->searchField["SCORE".$i]."点";
                    if ($this->searchField["SCORE_S".$i] == "1") {
                        $setScoreSearch .= "に等しい";
                    } elseif ($this->searchField["SCORE_S".$i] == "2") {
                        $setScoreSearch .= "以上";
                    } else {
                        $setScoreSearch .= "以下";
                    }
                }

                if ($this->searchField["HYOUTEI".$i]) {
                    $setValueSearch  = "、学年成績";
                    $setValueSearch .= $this->searchField["HYOUTEI".$i];
                    if ($this->searchField["HYOUTEI_S".$i] == "1") {
                        $setValueSearch .= "に等しい";
                    } elseif ($this->searchField["HYOUTEI_S".$i] == "2") {
                        $setValueSearch .= "以上";
                    } else {
                        $setValueSearch .= "以下";
                    }
                }
                $contents .= $setSubclassName.$setSubSearch.$setReportSearch.$setSchoolingSearch.$setScoreSearch.$setValueSearch."\n";
            }
        }

        //ヘッダ
        $contents .= implode($header, ",")."\n";
        $schregNos = explode(",", $this->selectleft);
        foreach ($schregNos as $key => $val) {
            if ($this->type_div == "3") {
                list($nenkumi, $schNo) = explode("-", $val);
                $val = $schNo;
            }
            $query = knjm550mQuery::selectCsvQuery($this, $val);
            $schInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $contents .= implode($schInfo, ",")."\n";
            $header_data = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "生徒名簿";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
