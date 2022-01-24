<?php

require_once('for_php7.php');

    //定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knjf301Model extends Model {
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

    function knjf301Model() {
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
        $this->sec_competence = common::SecurityCheck($this->staffcd,"KNJF301");

        $temp = array("NUTRITIONCD01" => VARS::post("NUTRITIONCD01"),
                      "NUTRITIONCD02" => VARS::post("NUTRITIONCD02"),
                      "NUTRITIONCD03" => VARS::post("NUTRITIONCD03"),
                      "SPINERIBCD01" => VARS::post("SPINERIBCD01"),
                      "SPINERIBCD02" => VARS::post("SPINERIBCD02"),
                      "SPINERIBCD03" => VARS::post("SPINERIBCD03"),
                      "SPINERIBCD99" => VARS::post("SPINERIBCD99"),
                      "SKINDISEASECD01" => VARS::post("SKINDISEASECD01"),
                      "SKINDISEASECD02" => VARS::post("SKINDISEASECD02"),
                      "SKINDISEASECD03" => VARS::post("SKINDISEASECD03"),
                      "SKINDISEASECD99" => VARS::post("SKINDISEASECD99"),
                      "OTHERDISEASECD01" => VARS::post("OTHERDISEASECD01"),
                      "OTHERDISEASECD02" => VARS::post("OTHERDISEASECD02"),
                      "OTHERDISEASECD03" => VARS::post("OTHERDISEASECD03"),
                      "OTHERDISEASECD04" => VARS::post("OTHERDISEASECD04"),
                      "OTHERDISEASECD05" => VARS::post("OTHERDISEASECD05"),
                      "OTHERDISEASECD99" => VARS::post("OTHERDISEASECD99")
                        );
        $this->fields = array_merge($this->fields,$temp);

        //V_SCHOOL_MSTから学校コードを取得
        $rtnRow = knjf301Query::getSchoolMst();
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
        knjf301Query::getUpdateQuery($this);
        return;
    }

    //確定
    function &getFixedUpdateModel() {
        $this->fixedUpdate();
        $this->setMessage("MSG201");
    }

    //アップデート
    function &fixedUpdate() {
        knjf301Query::getFixedUpdateQuery($this);
        return;
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・疾病等結果一覧テーブル
        //学校側・報告テーブル
        knjf301Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        $header = array();
        $header[] = "";
        $header[] = "";
        $header[] = "栄養状態";
        $header[] = "";
        $header[] = "";
        $header[] = "脊柱・胸郭";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "皮膚疾患";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "その他疾病および異常";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";
        $header[] = "";

        $header2 = array();
        $header2[] = "学年";
        $header2[] = "性別";
        $header2[] = "正常範囲";
        $header2[] = "栄養不良要注意";
        $header2[] = "肥満傾向要注意";
        $header2[] = "正常範囲";
        $header2[] = "はと胸";
        $header2[] = "側弯";
        $header2[] = "その他";
        $header2[] = "正常範囲";
        $header2[] = "温疹";
        $header2[] = "アトピー性皮膚炎";
        $header2[] = "その他";
        $header2[] = "正常範囲";
        $header2[] = "喘息";
        $header2[] = "急性賢炎";
        $header2[] = "慢性賢炎";
        $header2[] = "ネフローゼ症候群";
        $header2[] = "その他";

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";
        $contents .= implode($header2, ",")."\n";

        $header_data = false;
        //DB接続
        $db = Query::dbCheckOut();
        $table = "";
        $where = "";
        $query = knjf301Query::selectCsvQuery($this);
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
            $couse = "疾病等結果一覧";
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
