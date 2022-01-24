<?php

require_once('for_php7.php');

class knjg081aModel extends Model {

    var $field = array();
    var $data  = array();
    var $cmd;

    function knjg081aModel()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("DATE"           =>  VARS::post("DATE"),            //処理年月日
                            "YEAR"            =>  VARS::post("YEAR"),            //年度
                            "GAKKI"           =>  VARS::post("GAKKI"),           //学期コード
                            "OUTPUT"          =>  VARS::post("OUTPUT"),          //調査書種類
                            "OS"              =>  VARS::post("OS"),              //所見入力時ＯＳ
                            "KANJI"           =>  VARS::post("KANJI"),           //漢字氏名印刷
                            "SONOTAJUUSYO"    =>  VARS::post("SONOTAJUUSYO"),    //その他住所を優先して印字する
                            "MIRISYU"         =>  VARS::post("MIRISYU"),         //未履修科目出力
                            "RISYU"           =>  VARS::post("RISYU"),           //履修のみ科目出力
                            "TANIPRINT_SOUGOU"  => VARS::post("TANIPRINT_SOUGOU"), //総合的な学習の時間の単位を０表示ラジオ
                            "TANIPRINT_RYUGAKU" => VARS::post("TANIPRINT_RYUGAKU"),//留学の単位を０表示ラジオ
                            "FORM6"           =>  VARS::post("FORM6"),           //フォーム選択
                            "useSyojikou3"    =>  VARS::post("useSyojikou3"),    //指導上参考となる諸事欄、３分割フォーム
                            "tyousasyoNotPrintAnotherAttendrec"    =>  VARS::post("tyousasyoNotPrintAnotherAttendrec"),    //出欠の前籍校を含まないチェックボックスを作成する
                            "KOTYO"           =>  VARS::post("KOTYO"),           //校長印刷
                            "GRADE_HR_CLASS"  =>  VARS::post("GRADE_HR_CLASS"),  //年組
                            "HYOTEI"          =>  VARS::post("HYOTEI"),          //評定の処理設定
                            "COMMENT"         =>  VARS::post("COMMENT"),         //学習成績概評
                            "SEKI"            =>  VARS::post("SEKI"),            //記載責任者
                            "DBNAME"          =>  VARS::post("DBNAME")           //DB変更に備えて
                     );
        $this->cmd     = VARS::request("cmd");
        $db = Query::dbCheckOut();
        $query = knjg081aQuery::getSchoolName();
        $this->school_name = $db->getOne($query);
        Query::dbCheckIn($db);
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期値の設定
        $this->Properties["useSyojikou3"]                      = ($this->Properties["useSyojikou3"] == '1')               ? $this->Properties["useSyojikou3"]                      : 0;
        $this->Properties["tyousasyoTaniPrint"]                = ($this->Properties["tyousasyoTaniPrint"] == '2')         ? $this->Properties["tyousasyoTaniPrint"]                : 1;
        $this->Properties["useCertifSchPrintCnt"]              = ($this->Properties["useCertifSchPrintCnt"] == '1')       ? $this->Properties["useCertifSchPrintCnt"]              : 0;
        $this->Properties["nenYoForm"]                         = ($this->Properties["nenYoForm"])                         ? $this->Properties["nenYoForm"]                         : "6";

    }

    //入力項目チェック
    function validateInput()
    {
        //必須入力チェック
        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //桁数チェック
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if (strlen($this->fields["PRINT_CNT"][$i]) > 2) {
                $this->setWarning("MSG915","( 発行枚数は2バイトまでです )");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel()
    {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $term_name = "PRINT_CNT"."-".$i;
            $this->fields["PRINT_CNT"][] = VARS::post($term_name);
        }

        if ($this->validateInput()) {
            knjg081aQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
