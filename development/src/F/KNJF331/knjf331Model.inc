<?php

require_once('for_php7.php');

    //定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knjf331Model extends Model {
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
    var $docNumber;

    function knjf331Model() {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");
        $this->gc_select    = VARS::post("gc_select");        //プルダウンメニュー
        $this->hyoteiYomikae = VARS::post("hyoteiYomikae");
        $this->execute_date = VARS::post("EXECUTE_DATE");
        $this->dataDiv      = VARS::post("DATA_DIV");
        $this->fixedDate    = VARS::post("FIXED_DATE");
        $this->fixedData    = VARS::post("FIXED_DATA");
        $this->docNumber    = VARS::post("DOC_NUMBER");

        if ($this->dataDiv != "2") {
            $this->dataField = array("000_01",
                                     "000_02",
                                     "001_01",
                                     "001_02",
                                     "002_01",
                                     "002_02",
                                     "003_01",
                                     "003_02",
                                     "003_03",
                                     "003_04",
                                     "003_05",
                                     "004_01",
                                     "004_02",
                                     "004_03",
                                     "005_01",
                                     "006_01",
                                     "006_02",
                                     "006_03",
                                     "007_01",
                                     "010_01",
                                     "011_01",
                                     "011_02",
                                     "012_01",
                                     "013_01",
                                     "014_01",
                                     "014_02",
                                     "015_01",
                                     "016_01");
        } else {
            $this->dataField = array("000_03",
                                     "000_04",
                                     "008_01",
                                     "008_02",
                                     "008_03",
                                     "008_04",
                                     "008_05",
                                     "009_01",
                                     "009_02",
                                     "009_03",
                                     "009_04",
                                     "009_05",
                                     "009_06",
                                     "009_07",
                                     "009_08",
                                     "009_09",
                                     "009_10");
        }

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd,"KNJF331");

        $this->updField = array();
        for ($i = 0; $i < get_count($this->fields["CODE"]); $i++) {
            $code_arr = explode("-", $this->fields["CODE"][$i]);
            foreach ($this->dataField as $key => $divSeq) {
                $setDivSeq = $divSeq."_".$code_arr[0]."_".$code_arr[1];
                $this->updField[$setDivSeq] = VARS::post("DATA{$setDivSeq}");
            }
        }

        //V_SCHOOL_MSTから学校コードを取得
        $this->schoolcd = knjf331Query::getSchoolMst();

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
        knjf331Query::getUpdateQuery($this);
        return;
    }

    //確定
    function &getFixedUpdateModel() {
        $this->fixedUpdate();
        $this->setMessage("MSG201");
    }

    //アップデート
    function &fixedUpdate() {
        knjf331Query::getFixedUpdateQuery($this);
        return;
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・疾病等結果一覧テーブル
        //学校側・報告テーブル
        knjf331Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        $header = array();
        if ($this->dataDiv == "1") {
            $header[] = "";
            $header[] = "";
            $header[] = "ア　栄養状態";
            $header[] = "";
            $header[] = "イ　脊柱・胸郭";
            $header[] = "";
            $header[] = "ウ　裸眼視力";
            $header[] = "";
            $header[] = "";
            $header[] = "";
            $header[] = "";
            $header[] = "エ　眼の疾病・異常";
            $header[] = "";
            $header[] = "";
            $header[] = "";
            $header[] = "カ　耳鼻咽頭疾患";
            $header[] = "";
            $header[] = "";
            $header[] = "";
            $header[] = "";
            $header[] = "サ　結核検診";
            $header[] = "";
            $header[] = "";
            $header[] = "";
            $header[] = "セ　尿";
            $header[] = "";
            $header[] = "";
            $header[] = "";

            $header2 = array();
            $header2[] = "年齢";
            $header2[] = "性別";
            $header2[] = "在籍者数";
            $header2[] = "受検者数";
            $header2[] = "栄養不良";
            $header2[] = "肥満傾向";
            $header2[] = "側わん";
            $header2[] = "その他";
            $header2[] = "Ａ";
            $header2[] = "Ｂ";
            $header2[] = "Ｃ";
            $header2[] = "Ｄ";
            $header2[] = "Ｂ+Ｃ+Ｄ";
            $header2[] = "感染性眼疾患";
            $header2[] = "色覚異常の疑い";
            $header2[] = "その他の眼疾患";
            $header2[] = "オ　難聴";
            $header2[] = "耳疾患";
            $header2[] = "鼻副鼻腔疾患";
            $header2[] = "口腔咽喉頭疾患異常";
            $header2[] = "キ　感染性皮膚疾患";
            $header2[] = "コ　結核";
            $header2[] = "対策委員会での要検討";
            $header2[] = "要精密検査";
            $header2[] = "シ　心臓の疾病異常";
            $header2[] = "ス　心電図異常";
            $header2[] = "蛋白検出";
            $header2[] = "糖検出";
            $header2[] = "ソ　寄生虫卵保有";
            $header2[] = "タ　その他の疾病異常";
        } else {
            $header[] = "";
            $header[] = "";
            $header[] = "ク　歯・口腔（1）";
            $header[] = "";
            $header[] = "";
            $header[] = "";
            $header[] = "";
            $header[] = "ケ　歯・口腔（2）";
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
            $header2[] = "在籍者数";
            $header2[] = "受検者数";
            $header2[] = "う歯処置完了Ａ";
            $header2[] = "う歯未処置Ｂ";
            $header2[] = "Ａ+Ｂ";
            $header2[] = "その他の歯疾患";
            $header2[] = "口腔の疾病異常";
            $header2[] = "COのある者";
            $header2[] = "GOのある者";
            $header2[] = "顎関節２";
            $header2[] = "咬合歯列２";
            $header2[] = "歯肉２";
            $header2[] = "Ｐ対象者";
            $header2[] = "Ｄ未処置歯数";
            $header2[] = "Ｍ喪失歯数";
            $header2[] = "Ｆ処置歯数";
            $header2[] = "ＤＭＦＴ指数";
        }

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";
        $contents .= implode($header2, ",")."\n";

        $header_data = false;
        //DB接続
        $db = Query::dbCheckOut();
        $table = "";
        $where = "";
        $query = knjf331Query::selectCsvQuery($this);
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
            $couse = "健康診断入力Ａ";
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
