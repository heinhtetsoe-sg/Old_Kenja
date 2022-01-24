<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjb3045Model extends Model {
    var $field      = array();
    var $cmd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjb3045Model()
    {
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");

        $this->field = array(
              "YEAR_SEME"   => VARS::post("YEAR_SEME")
            , "PRESEQ"      => VARS::post("PRESEQ")
            , "BSCSEQ"      => VARS::post("BSCSEQ")
            , "BSCTITLE"    => VARS::post("BSCTITLE")
            , "PRESEQDATA"  => VARS::post("PRESEQDATA")
            , "PREV_BSCSEQ" => VARS::post("PREV_BSCSEQ")
        );

        if ($this->field["YEAR_SEME"]){
            $list = explode('-', $this->field["YEAR_SEME"]);
            $this->year = $list[0];
            $this->semester = $list[1];
        } else {
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイル
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    // 講座展開表からの基本時間割自動生成
    function &getUpdateModel() {

        //入力チェック
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        //データの書込み処理
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = knjb3045Query::update($db, $this);
        // $result = true;

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);

        return $result;
    }

}
//var_dump($_REQUEST);
?>
