<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp852Model extends Model
{
    public $field      = array();
    public $cmd;
    public $subclassGroup;
    public $control;        //コントロールマスタ
    public $schoolName;
    public $schoolCode;

    /**
     * メンバ変数の初期化
     */
    public function knjp852Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"               =>  VARS::post("YEAR"),                //年度
                             "GRADE"              =>  VARS::post("GRADE"),               //学年
                             "HR_CLASS"           =>  VARS::post("HR_CLASS"),            //クラス
                             "CATEGORY_IS_CLASS"  =>  VARS::post("CATEGORY_IS_CLASS"),   //表示区分
                             "OUTPUT_DIV"         =>  VARS::post("OUTPUT_DIV"),          //出力対象
                             "ADDRESS_DIV"        =>  VARS::post("ADDRESS_DIV"),         //宛先住所
                             "PRINT_DATE"         =>  VARS::post("PRINT_DATE"),          //帳票日付
                             "DOCUMENTCD"         =>  VARS::post("DOCUMENTCD"),          //出力文面
                             "DBNAME"             =>  VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //文面マスタ設定取得
        $db = Query::dbCheckOut();
        $this->docSizeProperties = array();
        $query = knjp852Query::getDocument($this);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->Properties["documentMstSize_{$row["VALUE"]}"] != "") {
                $size = $this->Properties["documentMstSize_{$row["VALUE"]}"];
            } else {
                $size = "40 * 6";
            }
            $this->docSizeProperties[$row["VALUE"]] = $size;
        }
        Query::dbCheckIn($db);
    }

    public function clean()
    {
    }
}
