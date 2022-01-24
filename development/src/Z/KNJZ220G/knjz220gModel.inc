<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz220gModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knjz220gModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::request("YEAR"),          //年度
                             "SUBCLASSCD"       =>  VARS::request("SUBCLASSCD"),    //科目
                             "SUBCLASSCNT"      =>  VARS::request("SUBCLASSCNT"),   //科目数
                             "TESTCD"           =>  VARS::request("TESTCD"),        //テスト種別
                             "DATA_DIV"         =>  VARS::post("DATA_DIV"),         //データ区分
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                            );

        $cnt = $this->field["SUBCLASSCNT"];
        for($i=1;$i<=$cnt;$i++){
            $this->field["SUBCLASS".$i]       =  VARS::post("SUBCLASS".$i);
            $this->field["ASSESS_TBL_DIV".$i] =  VARS::post("ASSESS_TBL_DIV".$i);
            $this->field["CALC_UNIT_DIV".$i]  =  VARS::post("CALC_UNIT_DIV".$i);
        }

        $this->cmd         = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //テキストエリア入力チェック
        $ii = $this->field["SUBCLASSCNT"];;
        for($i=1; $i<=$ii; $i++)
        {
            //空かどうか？
            if ($this->field["ASSESS_TBL_DIV".$i] == "" || $this->field["CALC_UNIT_DIV".$i] == "" ) {
                $this->setWarning("MSG301","評価テーブル・平均点計算単位");
                return false;
            }
            //1か2か？
            if ($this->field["ASSESS_TBL_DIV".$i] != "1" || $this->field["CALC_UNIT_DIV".$i] != "2" ) {
                $this->setWarning("MSG901","1もしくは2を入力してください。");
                return false;
            }
        }
        return true;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
            return true;
        }
        return false;
    }

    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel(){
        $db = Query::dbCheckOut();
        $pre_year = $this->field["YEAR"] - 1;
        $preYear_cnt = $db->getOne(knjz220gQuery::getCopyData($this, $pre_year, "cnt"));

        //前年度データチェック
        if ($preYear_cnt == 0) {
            $this->setWarning("MSG203", "前年度データがありません。");
            return false;  
        }
        Query::dbCheckIn($db);
        knjz220gQuery::getCopyQuery($this, $pre_year);
        $this->setMessage("MSG201");
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjz220gQuery::getInsertQuery($this);
        return $result;
    }

}
?>
