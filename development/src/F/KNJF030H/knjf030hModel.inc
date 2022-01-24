<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf030hModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $select_opt  = array(); //セレクトデータ
    var $selectleft;

    /**
     * メンバ変数の初期化
     */
    function knjf030hModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),         //年度
                            "GAKKI"             =>  VARS::post("GAKKI"),        //学期コード
                            "KUBUN"             =>  VARS::post("KUBUN"),        //クラス・個人表示指定
                            "GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),   //クラス
                            "DATE"              =>  VARS::post("DATE"),         //
                            "CHECK1"            =>  VARS::post("CHECK1"),       //健康診断票（一般）
                            "CHECK2"            =>  VARS::post("CHECK2"),       //健康診断票（歯・口腔）
                            "CHECK3"            =>  VARS::post("CHECK3"),       //眼科健診結果のお知らせ
                            "CHECK4"            =>  VARS::post("CHECK4"),       //視力検査結果のお知らせ
                            "CHECK5"            =>  VARS::post("CHECK5"),       //聴力検査結果のお知らせ
                            "CHECK6"            =>  VARS::post("CHECK6"),       //耳鼻咽頭検診結果のお知らせ
                            "CHECK7"            =>  VARS::post("CHECK7"),       //歯科検診検査結果のお知らせ
                            "CHECK8"            =>  VARS::post("CHECK8"),       //歯科検診検査結果のお知らせ（全員配布）
                            "CHECK9"            =>  VARS::post("CHECK9"),       //定期健康診断結果のお知らせ
                            "CHECK10"           =>  VARS::post("CHECK10"),      //内科検診結果のお知らせ
                            "CHECK11"           =>  VARS::post("CHECK11"),      //運動器検診結果のお知らせ
                            "CHECK12"           =>  VARS::post("CHECK12"),      //色覚検査結果のお知らせ
                            "CHECK13"           =>  VARS::post("CHECK13"),      //心臓検診結果のお知らせ
                            "CHECK14"           =>  VARS::post("CHECK14"),      //心臓検診精密検査のお願い（主治医）
                            "CHECK15"           =>  VARS::post("CHECK15"),      //胸部エックス線検診（要経過観察）
                            "CHECK16"           =>  VARS::post("CHECK16"),      //胸部エックス線検診（要精密検査）
                            "CHECK17"           =>  VARS::post("CHECK17"),      //胸部エックス線検診（要精密検査 主治医）
                            "CHECK18"           =>  VARS::post("CHECK18"),      //未検診のお知らせ（歯科）
                            "CHECK19"           =>  VARS::post("CHECK19"),      //未検診のお知らせ
                            "CHECK20"           =>  VARS::post("CHECK20"),      //尿検査のお知らせ
                            "NYOCHECK1"         =>  VARS::post("NYOCHECK1"),    //１次尿検査のお知らせ
                            "NYOCHECK2"         =>  VARS::post("NYOCHECK2"),    //１次尿検査結果のお知らせ
                            "NYOCHECK3"         =>  VARS::post("NYOCHECK3"),    //２次尿検査結果のお知らせ
                            "DBNAME"            =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class') $this->select_opt = array();

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
