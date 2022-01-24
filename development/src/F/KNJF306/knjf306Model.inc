<?php

require_once('for_php7.php');

    //定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knjf306Model extends Model {
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

    function knjf306Model() {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->field = array("DOC_NUMBER"   =>  VARS::post("DOC_NUMBER"),   //文書番号
                             "HIMAN"        =>  VARS::post("HIMAN"),        //肥満傾向
                             "SOSHIN"       =>  VARS::post("SOSHIN"),       //痩身傾向
                             "IDOU_DATE"    =>  VARS::post("IDOU_DATE"),    //異動対象日
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                             );

        $this->cmd           = VARS::request("cmd");
        $this->gc_select     = VARS::post("gc_select");        //プルダウンメニュー
        $this->execute_date  = VARS::post("EXECUTE_DATE");
        $this->fixedDate     = VARS::post("FIXED_DATE");
        $this->idouDate      = VARS::post("IDOU_DATE");
        $this->himan         = VARS::post("HIMAN");
        $this->soshin        = VARS::post("SOSHIN");
        $this->fixedData     = VARS::post("FIXED_DATA");

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd,"KNJF306");

        //V_SCHOOL_MSTから学校コードを取得
        $this->schoolcd = knjf306Query::getSchoolMst();

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();
        $checkCnt = $db->getOne(knjf306Query::getMedexamDetDat($this));
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
        knjf306Query::getUpdateQuery($this);
        return;
    }

    //確定
    function &getFixedUpdateModel() {
        $this->fixedUpdate();
        $this->setMessage("MSG201");
    }

    //アップデート
    function &fixedUpdate() {
        knjf306Query::getFixedUpdateQuery($this);
        return;
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //学校側・報告テーブル
        knjf306Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $db = Query::dbCheckOut();
        $this->shcoolName = $db->getOne(knjf306Query::getSchoolName($this));
        Query::dbCheckIn($db);

        $contents  = "";    //クリア
        $contents .= $this->shcoolName."\n";
        $contents .= "肥満傾向児出現率,,,,,,,,出力日：".CTRL_DATE."\n";

        $header = array();
        $header[] = "区分";
        $header[] = "";

        $header2 = array();
        $header2[] = "";
        $header2[] = "";

        //年齢カウント分のヘッダーをセット
        $db = Query::dbCheckOut();
        $query = knjf306Query::getAge($this);
        $age = $db->query($query);
        $this->age = array();
        while ($row = $age->fetchRow(DB_FETCHMODE_ASSOC)) {
            $header[] = $row["AGE"]."歳";
            $header[] = "";
            $header[] = "";
            $header2[] = "受験者数";
            $header2[] = "該当者数";
            $header2[] = "出現率（％）";
            $this->age[] = $row["AGE"]; //年齢を配列にセット
        }
        Query::dbCheckIn($db);

        $contents .= implode($header, ",")."\n";
        $contents .= implode($header2, ",")."\n";

        $header_data = false;
        //肥満傾向児出現率
        $db = Query::dbCheckOut();
        $query  = knjf306Query::selectCsvQuery($this, "001");
        $sum1 = $db->getRow(knjf306Query::selectCsvQuery2($this, "001"), DB_FETCHMODE_ASSOC);
        $sumFlg1 = false;
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= implode($row, ",") ."\n";
            $header_data = true;
            $sumFlg1 = true;
        }
        $result->free();
        if ($sumFlg1) {
            $contents .= implode($sum1, ",") ."\n";
        }
        Query::dbCheckIn($db);

        //痩身傾向児出現率
        $contents .= "\n痩身傾向児出現率\n";
        $contents .= implode($header, ",")."\n";
        $contents .= implode($header2, ",")."\n";
        $db = Query::dbCheckOut();
        $query = knjf306Query::selectCsvQuery($this, "002");
        $sum2 = $db->getRow(knjf306Query::selectCsvQuery2($this, "002"), DB_FETCHMODE_ASSOC);
        $sumFlg2 = false;
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= implode($row, ",") ."\n";
            $header_data = true;
            $sumFlg2 = true;
        }
        $result->free();
        if ($sumFlg2) {
            $contents .= implode($sum2, ",") ."\n";
        }
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "肥満度傾向児及び痩身傾向児出現率";
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
