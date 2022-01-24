<?php

require_once('for_php7.php');

    //定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knjf332Model extends Model {
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

    function knjf332Model() {
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

        $this->dataField = array("001_01",
                                 "001_02",
                                 "001_03",
                                 "001_04",
                                 "001_05",
                                 "001_06",
                                 "001_07",
                                 "001_08",
                                 "001_09",
                                 "002_01",
                                 "003_01",
                                 "004_01");

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJF332");

        $this->updField = array();
        for ($i = 0; $i < get_count($this->fields["CODE"]); $i++) {
            $code_arr = explode("-", $this->fields["CODE"][$i]);
            foreach ($this->dataField as $key => $divSeq) {
                $setDivSeq = $divSeq."_".$code_arr[0]."_".$code_arr[1];
                $this->updField[$setDivSeq] = VARS::post("DATA{$setDivSeq}");
            }
        }

        //V_SCHOOL_MSTから学校コードを取得
        $this->schoolcd = knjf332Query::getSchoolMst();

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
        knjf332Query::getUpdateQuery($this);
        return;
    }

    //確定
    function &getFixedUpdateModel() {
        $this->fixedUpdate();
        $this->setMessage("MSG201");
    }

    //アップデート
    function &fixedUpdate() {
        knjf332Query::getFixedUpdateQuery($this);
        return;
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・疾病等結果一覧テーブル
        //学校側・報告テーブル
        knjf332Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        $header = array();
        $header[] = "";
        $header[] = "";
        $header[] = "アレルギー疾患";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";

        $header2 = array();
        $header2[] = "年齢";
        $header2[] = "性別";
        $header2[] = "ぜん息";
        $header2[] = "ｱﾄﾋﾟｰ性皮膚炎";
        $header2[] = "ｱﾚﾙｷﾞｰ性鼻炎";
        $header2[] = "ｱﾚﾙｷﾞｰ性結膜炎";
        $header2[] = "化学物質過敏症";
        $header2[] = "食物ｱﾚﾙｷﾞｰ";
        $header2[] = "ｱﾅﾌｨﾗｷｼｰ";
        $header2[] = "エピペン保持者";
        $header2[] = "その他";
        $header2[] = "身体的疾患";
        $header2[] = "精神的疾患";
        $header2[] = "心身症及びその関連疾患";

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";
        $contents .= implode($header2, ",")."\n";

        $header_data = false;
        //DB接続
        $db = Query::dbCheckOut();
        $table = "";
        $where = "";
        $query = knjf332Query::selectCsvQuery($this);
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
