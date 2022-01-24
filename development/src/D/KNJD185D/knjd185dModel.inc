<?php

require_once('for_php7.php');

class knjd185dModel extends Model {
    var $field = array();
    var $cmd;
    var $selectdata;
    var $selectdataText;

    function knjd185dModel() {}

    function init() {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DISP"             =>  VARS::post("DISP"),             //クラス生徒区分
                             "DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "SEQ001"           =>  VARS::post("SEQ001"),           //帳票出力
                             "SEQ002"           =>  VARS::post("SEQ002"),           //科目
                             "SEQ003"           =>  VARS::post("SEQ003"),           //年組番／学籍番号
                             "SEQ004"           =>  VARS::post("SEQ004"),           //欠課時数
                             "SEQ005"           =>  VARS::post("SEQ005"),           //順位の基準点
                             "SEQ0061"          =>  VARS::post("SEQ0061"),          //平均・順位（学級）
                             "SEQ0062"          =>  VARS::post("SEQ0062"),          //平均・順位（コース）
                             "SEQ0063"          =>  VARS::post("SEQ0063"),          //平均・順位（学年）
                             "SEQ007"           =>  VARS::post("SEQ007"),           //特別活動の記録
                             "SEQ008"           =>  VARS::post("SEQ008"),           //総合的な学習の時間/検定
                             "SEQ009"           =>  VARS::post("SEQ009"),           //備考
                             "SEQ010"           =>  VARS::post("SEQ010"),           //定型コメント
                             "SEQ011"           =>  VARS::post("SEQ011"),           //未履修科目
                             "SEQ021"           =>  VARS::post("SEQ021"),           //増加単位
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataText   = VARS::post("selectdataText");

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
