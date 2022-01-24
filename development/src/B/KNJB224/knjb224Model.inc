<?php

require_once('for_php7.php');
class knjb224Model extends Model
{
    public $field      = array();
    public $selectdata = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $lastColumn;//CSVの最後の列の値

    public function knjb224Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->getPropertiesAll(); //プロパティファイル
        $this->year = CTRL_YEAR;
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力ラジオ 1:予定講座 2:講座
                             "DATE"             =>  VARS::post("DATE"),             //講座日付
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );
        $this->cmd = VARS::request("cmd");
        $this->selectdata = explode(",", VARS::post("selectdata"));

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function clean()
    {
    }

    public function getDownloadModel()
    {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        $data = false;
        //DB接続
        $db = Query::dbCheckOut();
        //学年配列
        $gradeArray = array();
        $query = knjb224Query::getRegdGdat($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $gradeArray[$row["GRADE"]] = $row["GRADE_NAME1"];
        }
        $result->free();
        //ヘッダ配列
        $header = array();
        $header[] = "科目コード";
        $header[] = "科目名";
        $header[] = "講座コード";
        $header[] = "講座名";
        $header[] = "単位";
        $header[] = "修得期";
        $header[] = "必修";
        $header[] = "時間";
        $header[] = "連続";
        $header[] = "履修登録人数";
        //Ｘ年生
        foreach ($gradeArray as $grade => $grade_name1) {
            $header[] = $grade_name1;
        }
        $header[] = "講座担当";
        $header[] = "施設";
        $header[] = "定員";
        $header[] = $this->lastColumn;
        //ヘッダ配列を出力変数にセット
        $headerContents = implode($header, $comma) .$lf;
        //学期配列
        $semArray = array();
        $query = knjb224Query::getSemester($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semArray[$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();
        //単位マスタ科目毎配列
        $creditArray = array();
        $query = knjb224Query::getCreditMst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $creditArray[$row["SUBCLASSCD"]] = $row;
        }
        $result->free();
        //データ
        $dataContents  = "";
        $query = knjb224Query::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換

            //必修
            $requireFlgMaru = ($creditArray[$row["SUBCLASSCD"]]["REQUIRE_FLG"] == "1" || $creditArray[$row["SUBCLASSCD"]]["REQUIRE_FLG"] == "2") ? "○" : "";
            //修得期
            $takesemes = ($row["TAKESEMES"] === "0") ? "通年" : $semArray[$row["TAKESEMES"]];
            //履修登録人数
            $stdArray = $this->makeChairStdDat($db, $row["CHAIRCD"]);
            $stdCnt = 0;
            foreach ($stdArray as $grade => $cnt) {
                $stdCnt += $cnt;
            }
            $stdCnt = ($stdCnt > 0) ? $stdCnt : "";
            //講座担当
            $stfArray = $this->makeChairStfDat($db, $row["CHAIRCD"]);
            //施設・定員
            $facArray = $this->makeChairFacDat($db, $row["CHAIRCD"]);

            $dataContents .= $row["SUBCLASSCD"] . $comma;
            $dataContents .= $row["SUBCLASSNAME"] . $comma;
            $dataContents .= $row["CHAIRCD"] . $comma;
            $dataContents .= $row["CHAIRNAME"] . $comma;
            $dataContents .= $creditArray[$row["SUBCLASSCD"]]["CREDITS"] . $comma;
            $dataContents .= $takesemes . $comma;
            $dataContents .= $requireFlgMaru . $comma;
            $dataContents .= $row["LESSONCNT"] . $comma;
            $dataContents .= $row["FRAMECNT"] . $comma;
            $dataContents .= $stdCnt . $comma;
            //Ｘ年生
            foreach ($gradeArray as $grade => $grade_name1) {
                $gradeMaru = ($stdArray[$grade]) ? "○" : "";
                $dataContents .= $gradeMaru . $comma;
            }
            $dataContents .= $stfArray["STAFFNAME"] . $comma;
            $dataContents .= $facArray["FACILITYNAME"] . $comma;
            $dataContents .= $facArray["CAPACITY"] . $comma;

            $dataContents .= $this->lastColumn . $lf;
            $data = true;
        }
        $result->free();
        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        $contents  = "";
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $title = ($this->field["OUTPUT"] == '2') ? "講座情報一覧" : "予定講座情報一覧";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    public function makeChairStdDat($db, $rowChaircd)
    {
        $rtnArray = array();

        if (strlen($rowChaircd)) {
            $query = knjb224Query::getChairStdDat($this, $rowChaircd);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rtnArray[$row["GRADE"]] = $row["CNT"];
            }
            $result->free();
        }

        return $rtnArray;
    }

    public function makeChairStfDat($db, $rowChaircd)
    {
        $rtnArray = array();

        if (strlen($rowChaircd)) {
            $seq = "";
            $query = knjb224Query::getChairStfDat($this, $rowChaircd);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rtnArray["STAFFNAME"] .= $seq . $row["STAFFNAME"];
                $seq = ".";
            }
            $result->free();
        }

        return $rtnArray;
    }

    public function makeChairFacDat($db, $rowChaircd)
    {
        $rtnArray = array();

        if (strlen($rowChaircd)) {
            $seq = "";
            $query = knjb224Query::getChairFacDat($this, $rowChaircd);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rtnArray["FACILITYNAME"] .= $seq . $row["FACILITYNAME"];
                $rtnArray["CAPACITY"] .= $seq . $row["CAPACITY"];
                $seq = ".";
            }
            $result->free();
        }

        return $rtnArray;
    }
}
