<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje991tModel extends Model {
    var $field      = array();
    var $select_data     = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knje991tModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("DATE"           =>  VARS::post("DATE"),            //処理年月日
                            "YEAR"            =>  VARS::post("YEAR"),            //年度
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
                            "GRADE_HR_CLASS"  =>  VARS::post("GRADE_HR_CLASS"),  //年組
                            "HYOTEI"          =>  VARS::post("HYOTEI"),          //評定の処理設定
                            "COMMENT"         =>  VARS::post("COMMENT"),         //学習成績概評
                            "DBNAME"          =>  VARS::post("DBNAME")           //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期値の設定
        $this->Properties["useSyojikou3"]                      = ($this->Properties["useSyojikou3"] == '1')               ? $this->Properties["useSyojikou3"]                      : 0;
        $this->Properties["tyousasyoTaniPrint"]                = ($this->Properties["tyousasyoTaniPrint"] == '2')         ? $this->Properties["tyousasyoTaniPrint"]                : 1;
        $this->Properties["useCertifSchPrintCnt"]              = ($this->Properties["useCertifSchPrintCnt"] == '1')       ? $this->Properties["useCertifSchPrintCnt"]              : 0;
        $this->Properties["tyousasyoAttendrecRemarkFieldSize"] = ($this->Properties["tyousasyoAttendrecRemarkFieldSize"]) ? $this->Properties["tyousasyoAttendrecRemarkFieldSize"] : "";
        $this->Properties["train_ref_1_2_3_field_size"]        = ($this->Properties["train_ref_1_2_3_field_size"])        ? $this->Properties["train_ref_1_2_3_field_size"]        : "";
        $this->Properties["nenYoForm"]                         = ($this->Properties["nenYoForm"])                         ? $this->Properties["nenYoForm"]                         : "6";
        $this->Properties["tyousasyoSougouHyoukaNentani"]      = ($this->Properties["tyousasyoSougouHyoukaNentani"])      ? $this->Properties["tyousasyoSougouHyoukaNentani"]      : "";
        $this->Properties["tyousasyoKinsokuForm"]              = ($this->Properties["tyousasyoKinsokuForm"])              ? $this->Properties["tyousasyoKinsokuForm"]              : "";
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
