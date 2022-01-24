<?php

require_once('for_php7.php');

    //定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knjf307Model extends Model {
    var $cmd;
    var $field;
    var $gc_select;                     //セレクトメニュー
    var $control_data = array();        //コントロールのデータを保持
    var $staffcd;                       //セキュリティーチェック
    var $sec_competence;                //権限保持
    var $updated = array();             //UPDATED値保持
    var $fields = array();
    var $schoolcd;                      //学校コード
    var $execute_date;                  //県への報告用登録日付

    function knjf307Model() {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->field = array("DOC_NUMBER"   =>  VARS::post("DOC_NUMBER"),   //文書番号
                             "IDOU_DATE"    =>  VARS::post("IDOU_DATE"),    //異動対象日
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                             );

        $this->cmd           = VARS::request("cmd");
        $this->gc_select     = VARS::post("gc_select");        //プルダウンメニュー
        $this->execute_date  = VARS::post("EXECUTE_DATE");
        $this->fixedDate     = VARS::post("FIXED_DATE");
        $this->idouDate      = VARS::post("IDOU_DATE");
        $this->fixedData     = VARS::post("FIXED_DATA");

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd,"KNJF307");

        //V_SCHOOL_MSTから学校コードを取得
        $this->schoolcd = knjf307Query::getSchoolMst();

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();
        $checkCnt = $db->getOne(knjf307Query::getMedexamDetDat($this));
        Query::dbCheckIn($db);
        if ($checkCnt == "") {
            $this->setWarning("MSG303","(定期健康診断情報)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
            return true;
        }
        return false;
    }

    //アップデート時の内部メソッド
    function &update() {
        knjf307Query::getUpdateQuery($this);
        return;
    }

    //確定
    function &getFixedUpdateModel() {
        $this->fixedUpdate();
        $this->setMessage("MSG201");
    }

    //アップデート
    function &fixedUpdate() {
        knjf307Query::getFixedUpdateQuery($this);
        return;
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //学校側・報告テーブル
        knjf307Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $db = Query::dbCheckOut();
        $this->shcoolName = $db->getOne(knjf307Query::getSchoolName($this));
        Query::dbCheckIn($db);

        $contents  = "";    //クリア
        $contents .= $this->shcoolName."\n";
        $contents .= "身体測定値平均値一覧,,,,出力日：".CTRL_DATE."\n";

        $headerH = array();
        $headerH[] = "身長";

        $headerW = array();
        $headerW[] = "体重";

        $headerS = array();
        $headerS[] = "座高";

        $header2 = array();
        $header2[] = "区分";
        $header2[] = "";

        $header3 = array();
        $header3[] = "";
        $header3[] = "";

        //年齢カウント分のヘッダーをセット
        $db = Query::dbCheckOut();
        $query = knjf307Query::getAge($this);
        $age = $db->query($query);
        $this->age = array();
        while ($row = $age->fetchRow(DB_FETCHMODE_ASSOC)) {
            $header2[] = $row["AGE"]."歳";
            $header2[] = "";
            $header2[] = "";
            $header3[] = "受検者数";
            $header3[] = "総和";
            $header3[] = "平均";
            $this->age[] = $row["AGE"]; //年齢を配列にセット
        }
        Query::dbCheckIn($db);

        $header_data = false;
        //身体測定値平均値一覧
        $db = Query::dbCheckOut();
        $coursecd = array();
        $query = knjf307Query::getCoursecd();
        $result = $db->query($query);
        $coursecd = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $coursecd[] .= $row["COURSECD"];
        }
        $query  = knjf307Query::selectCsvQuery($this, "003", $coursecd);
        $seqFlg  = "";
        $sumFlg3 = false;
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SEQ"] == "01") {
                if ($seqFlg != $row["SEQ"]) {
                    $contents .= implode($headerH, ",")."\n";
                    $contents .= implode($header2, ",")."\n";
                    $contents .= implode($header3, ",")."\n";
                }
                $dataTmp   = array();
                $dataTmp[] = $row["COURSENAME"];
                $dataTmp[] = $row["SEXNAME"];
                foreach ($this->age as $ageVal) {
                    $dataTmp[] = $row["INT_VAL1_".$ageVal];
                    $dataTmp[] = $row["INT_VAL2_".$ageVal];
                    $dataTmp[] = round($row["AVG_".$ageVal], 2);
                }
            } else if ($row["SEQ"] == "02") {
                if ($seqFlg != $row["SEQ"]) {
                    $contents .= "\n";
                    $contents .= implode($headerW, ",")."\n";
                    $contents .= implode($header2, ",")."\n";
                    $contents .= implode($header3, ",")."\n";
                }
                $dataTmp   = array();
                $dataTmp[] = $row["COURSENAME"];
                $dataTmp[] = $row["SEXNAME"];
                foreach ($this->age as $ageVal) {
                    $dataTmp[] = $row["INT_VAL1_".$ageVal];
                    $dataTmp[] = $row["INT_VAL2_".$ageVal];
                    $dataTmp[] = round($row["AVG_".$ageVal], 2);
                }
            } else if ($row["SEQ"] == "03") {
                if ($seqFlg != $row["SEQ"]) {
                    $contents .= "\n";
                    $contents .= implode($headerS, ",")."\n";
                    $contents .= implode($header2, ",")."\n";
                    $contents .= implode($header3, ",")."\n";
                }
                $dataTmp   = array();
                $dataTmp[] = $row["COURSENAME"];
                $dataTmp[] = $row["SEXNAME"];
                foreach ($this->age as $ageVal) {
                    $dataTmp[] = $row["INT_VAL1_".$ageVal];
                    $dataTmp[] = $row["INT_VAL2_".$ageVal];
                    $dataTmp[] = round($row["AVG_".$ageVal], 2);
                }
            }
            $dataTmp = str_replace(",",".",$dataTmp);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= implode($dataTmp, ",") ."\n";
            $seqFlg    = $row["SEQ"];

            $header_data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "身体測定値平均値一覧";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
