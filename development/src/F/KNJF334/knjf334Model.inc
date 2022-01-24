<?php

require_once('for_php7.php');

    //定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knjf334Model extends Model {
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
    var $firstFlg;

    function knjf334Model() {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");
        $this->gc_select    = VARS::post("gc_select");        //プルダウンメニュー
        $this->hyoteiYomikae    = VARS::post("hyoteiYomikae");
        $this->execute_date = VARS::post("EXECUTE_DATE");
        $this->dataDiv = VARS::post("DATA_DIV");
        $this->fixedDate = VARS::post("FIXED_DATE");
        $this->fixedData = VARS::post("FIXED_DATA");
        $this->firstFlg = VARS::post("FIRST_FLG") ? VARS::post("FIRST_FLG") : "1";
        if ($this->cmd == "reset") {
            $this->firstFlg = "1";
        }

        $this->dataField = array("000_01",
                                 "001_02",
                                 "001_03",
                                 "001_04",
                                 "010_05",
                                 "011_06",
                                 "011_07",
                                 "011_08");

        $this->iptField = array("001_02",
                                "001_03",
                                "001_04");

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJF334");

        $this->updField = array();
        $this->hidField = array();
        for ($i = 0; $i < get_count($this->fields["CODE"]); $i++) {
            $code_arr = explode("-", $this->fields["CODE"][$i]);
            foreach ($this->dataField as $key => $divSeq) {
                $setDivSeq = $divSeq."_".$code_arr[0];
                $this->updField[$setDivSeq] = get_count(VARS::post("DATA{$setDivSeq}")) > 0 ? VARS::post("DATA{$setDivSeq}")[0] : VARS::post("DATA{$setDivSeq}");
                $hid1Seq = "GCD_DATA".$setDivSeq;
                $this->hidField[$hid1Seq] = VARS::post("HID_{$hid1Seq}") ? VARS::post("HID_{$hid1Seq}") : "99";  //NOT_NULLなので、意味のない値を設定
                $hid2Seq = "SCHKIND_DATA".$setDivSeq;
                $this->hidField[$hid2Seq] = VARS::post("HID_{$hid2Seq}") ? VARS::post("HID_{$hid2Seq}") : "ZZ";  //NOT_NULLなので、意味のない値を設定
            }
        }

        //V_SCHOOL_MSTから学校コードを取得
        $this->schoolcd = knjf334Query::getSchoolMst();

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->update();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    function &update() {
        knjf334Query::getUpdateQuery($this);
        return;
    }

    //確定
    function &getFixedUpdateModel() {
        $this->fixedUpdate();
        $this->setMessage("MSG201");
    }

    //アップデート
    function &fixedUpdate() {
        knjf334Query::getFixedUpdateQuery($this);
        return;
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・疾病等結果一覧テーブル
        //学校側・報告テーブル
        knjf334Query::getUpdateReport($this);
        $chkVal = knjf334Query::getE045PrgData($this);                     // 名称MのNAME3にKNJF334の登録が無い
        $retCnt = knjf334Query::getSearchReportSchoolCnt($this, $chkVal);  // AFT_SEARCH_REPORT_SCHOOL_DATに登録が無い。
        if ($chkVal == "" || $retCnt == 0) {
            $this->setMessage("MSG303", "委員会側のテーブルに学校データが登録されていないため、一部データが登録できませんでした。");
        } else {
            $this->setMessage("MSG201");
        }
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        $header = array();
        $header[] = "";
        $header[] = "";
        $header[] = "問診調査";
        $header[] = "";
        $header[] = "";
        $header[] = "学校医による診察";
        $header[] = "";
        $header[] = "";
        $header[] = "合計";

        $header2 = array();
        $header2[] = "";
        $header2[] = "在籍";
        $header2[] = "問診調査";
        $header2[] = "問診調査の結果";
        $header2[] = "";
        $header2[] = "診察実施";
        $header2[] = "診察の結果";
        $header2[] = "";
        $header2[] = "結核対策委員会";
 
        $header3 = array();
        $header3[] = "学年";
        $header3[] = "者数";
        $header3[] = "実施者数";
        $header3[] = "検討不要者数";
        $header3[] = "要検討者数";
        $header3[] = "者数";
        $header3[] = "異常なし";
        $header3[] = "要検診者数";
        $header3[] = "要検討者数";

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";
        $contents .= implode($header2, ",")."\n";
        $contents .= implode($header3, ",")."\n";

        $header_data = false;
        //DB接続
        $db = Query::dbCheckOut();
        $table = "";
        $where = "";
        $query = knjf334Query::selectCsvQuery($this);
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
            $couse = "健康診断入力Ｂ";
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
