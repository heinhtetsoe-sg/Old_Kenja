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

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・疾病等結果一覧テーブル
        //学校側・報告テーブル
        knjf301Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }
}
?>
