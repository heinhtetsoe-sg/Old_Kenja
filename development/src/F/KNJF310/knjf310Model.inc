<?php

require_once('for_php7.php');

    //定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knjf310Model extends Model {
    var $cmd;
    var $gc_select;                     //セレクトメニュー
    var $control_data = array();        //コントロールのデータを保持
    var $staffcd;                       //セキュリティーチェック
    var $sec_competence;                //権限保持
    var $updated = array();             //UPDATED値保持
    var $fields = array();
    var $hyoteiYomikae; // 評定1の場合は2で処理する
    var $schoolcd;  //学校コード
    var $execute_date; //県への報告用登録日付

    function knjf310Model() {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");
        $this->gc_select    = VARS::post("gc_select");        //プルダウンメニュー
        $this->hyoteiYomikae    = VARS::post("hyoteiYomikae");
        $this->execute_date = VARS::post("EXECUTE_DATE");
        $this->fixedDate = VARS::post("FIXED_DATE");
        $this->fixedData = VARS::post("FIXED_DATA");

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd,"KNJF310");

        $nameArray = array("REMARK101" => VARS::post("REMARK101")
                          ,"REMARK102" => VARS::post("REMARK102")
                          ,"REMARK103" => VARS::post("REMARK103")
                          ,"REMARK104" => VARS::post("REMARK104")
                          ,"REMARK105" => VARS::post("REMARK105")
                          ,"REMARK106" => VARS::post("REMARK106")
                          ,"REMARK107" => VARS::post("REMARK107")
                          ,"REMARK108" => VARS::post("REMARK108")
                          ,"REMARK201" => VARS::post("REMARK201")
                          ,"REMARK202" => VARS::post("REMARK202")
                          ,"REMARK203" => VARS::post("REMARK203")
                          ,"REMARK204" => VARS::post("REMARK204")
                          ,"REMARK205" => VARS::post("REMARK205")
                          ,"REMARK206" => VARS::post("REMARK206")
                          ,"REMARK207" => VARS::post("REMARK207")
                          ,"REMARK208" => VARS::post("REMARK208")
                          ,"REMARK209" => VARS::post("REMARK209")
                          ,"REMARK210" => VARS::post("REMARK210")
                          ,"REMARK211" => VARS::post("REMARK211")
                          ,"REMARK212" => VARS::post("REMARK212"));
        $this->fields = array_merge($this->fields,$nameArray);

        //V_SCHOOL_MSTから学校コードを取得
        $rtnRow = knjf310Query::getSchoolMst();
        $this->schoolcd = $rtnRow["KYOUIKU_IINKAI_SCHOOLCD"];

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->update();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    function &update() {
        knjf310Query::getUpdateQuery($this);
        return;
    }

    //確定
    function &getFixedUpdateModel() {
        $this->fixedUpdate();
        $this->setMessage("MSG201");
    }

    //アップデート
    function &fixedUpdate() {
        knjf310Query::getFixedUpdateQuery($this);
        return;
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・疾病等結果一覧テーブル
        //学校側・報告テーブル
        knjf310Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        $header = array();
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "検査結果";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "歯列・咬合";
        $header[] = "";
        $header[] = "";
        $header[] = "顎関節";
        $header[] = "";
        $header[] = "";
        $header[] = "歯垢の状態";
        $header[] = "";
        $header[] = "";
        $header[] = "歯肉の状態";
        $header[] = "";
        $header[] = "";

        $header2 = array();
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "う歯の状況";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";
        $header2[] = "";

        $header3 = array();
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "内訳";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";
        $header3[] = "";

        $header4 = array();
        $header4[] = "学年";
        $header4[] = "性別";
        $header4[] = "受験者数";
        $header4[] = "う歯のなし";
        $header4[] = "う歯のあり";
        $header4[] = "要治療う歯あり";
        $header4[] = "う歯処置完了";
        $header4[] = "要注意乳歯あり";
        $header4[] = "要観察歯あり";
        $header4[] = "喪失歯あり";
        $header4[] = "異常なし";
        $header4[] = "定期的観察が必要";
        $header4[] = "定期医による診断が必要";
        $header4[] = "異常なし";
        $header4[] = "定期的観察が必要";
        $header4[] = "定期医による診断が必要";
        $header4[] = "ほとんど付着なし";
        $header4[] = "若干の付着あり";
        $header4[] = "相当の付着あり";
        $header4[] = "異常なし";
        $header4[] = "定期的観察が必要";
        $header4[] = "定期医による診断が必要";

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";
        $contents .= implode($header2, ",")."\n";
        $contents .= implode($header3, ",")."\n";
        $contents .= implode($header4, ",")."\n";

        $header_data = false;
        //DB接続
        $db = Query::dbCheckOut();
        $table = "";
        $where = "";
        $query = knjf310Query::selectCsvQuery($this);
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "歯科検診結果報告";
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
