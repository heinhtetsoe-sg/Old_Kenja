<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd030Model extends Model {
    var $field      = array();
    var $cmd;
    var $sort;//ソート
    var $schregno;//学籍番号
    var $chaircd;   //講座番号
    var $score;   //得点
    var $file;
    var $choice;
    var $data_arr =array();
    var $perfect;
    var $schregno;
    var $attendno;
    var $operation_date;

    //メンバ変数の初期化
    function knjd030Model()
    {
        $this->sort["ATTENDNO"] = 1;
    }
    function init()
    {

        //学習記録エクスプローラより取得
        if (VARS::post("GTREDATA")){
            $this->field = unserialize(VARS::post("GTREDATA"));
        }

            $this->perfect = VARS::post("perfect");
            $this->operation_date = VARS::post("OPERATION_DATE");

        //初回表示
        if (VARS::get("start")){
            $this->field["CHAIRCD"] = array();
            $this->field["APPDATE"] = array();
        }

        //得点
        if (VARS::post("SCORE")){
            $this->field["SCORE"]   = VARS::post("SCORE");              
        }

        $this->cmd                  = VARS::request("cmd");
        $this->choice               = VARS::post("choice");

    }
    
    //全体表示用モデルを返します
    function &getMainModel()
    {
        //ソート用 番号、席次
        $sort = array("ATTENDNO","SCORE");
        //ソートが押下
        if (trim(VARS::request("sort")) != ""){
            foreach($sort as $field){
                if (VARS::request("sort") == $field
                        && is_numeric($this->sort[VARS::get("sort")])
                        && $this->sort[VARS::get("sort")] != 0) {
                    //昇順⇔降順
                    $this->sort[$field] = $this->sort[$field] *(-1);
                }elseif (VARS::get("sort") != $field) {
                    unset($this->sort[$field]);
                }else{
                    //昇順
                    $this->sort[$field] = 1;
                }
            }
        }
    }

    //入力項目チェック
    function validateInput()
    {
       if(!$this->field["CHAIRCD"]){
           $this->setWarning("MSG304");
           return false;
       }
       for($i = 0; $i<get_count($this->field["SCORE"]) ;$i++){
           if($this->perfect < $this->field["SCORE"][$i]){
               $this->setWarning("MSG901","得点が満点以上のデータが存在します。");
               return false;
           }
       }
        return true;
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
        }
    }
    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        //得点入力画面編集対象データの書込み処理
        $result = knjd030Query::getUpdateQuery($this);
        return $result;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
