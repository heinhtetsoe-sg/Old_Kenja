<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje075yModel extends Model {
    var $field      = array();
    var $select_data     = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $school_name;    //学校名

    /**
     * メンバ変数の初期化
     */
    function knje075yModel()
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
                            "GAKKI"           =>  VARS::post("GAKKI"),           //学期コード
                            "OUTPUT"          =>  VARS::post("OUTPUT"),          //調査書種類
                            "OS"              =>  VARS::post("OS"),              //所見入力時ＯＳ
                            "KANJI"           =>  VARS::post("KANJI"),           //漢字氏名印刷
                            "MIRISYU"         =>  VARS::post("MIRISYU"),         //未履修科目出力
                            "RISYU"           =>  VARS::post("RISYU"),           //履修のみ科目出力
                            "FORM6"           =>  VARS::post("FORM6"),           //フォーム選択
                            "useSyojikou3"    =>  VARS::post("useSyojikou3"),    //指導上参考となる諸事欄、３分割フォーム
                            "KOTYO"           =>  VARS::post("KOTYO"),           //校長印刷
                            "GRADE"           =>  VARS::post("GRADE"),           //学年
                            "GRADE_HR_CLASS"  =>  VARS::post("GRADE_HR_CLASS"),  //年組
                            "HYOTEI"          =>  VARS::post("HYOTEI"),          //評定の処理設定
                            "COMMENT"         =>  VARS::post("COMMENT"),         //学習成績概評
                            "SEKI"            =>  VARS::post("SEKI"),            //記載責任者
                            "DBNAME"          =>  VARS::post("DBNAME")           //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->cmd         = VARS::request("cmd");
        $db = Query::dbCheckOut();
        $query = knje075yQuery::getSchoolName();
        $this->school_name = $db->getOne($query);
        Query::dbCheckIn($db);

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->useSyojikou3       = ($this->Properties["useSyojikou3"]       == '1') ? $this->Properties["useSyojikou3"] : 0;
        $this->tyousasyoTaniPrint = ($this->Properties["tyousasyoTaniPrint"] == '2') ? $this->Properties["tyousasyoTaniPrint"] : 1;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
