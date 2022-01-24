<?php

require_once('for_php7.php');

//メニュー画面
class knjz320Model extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $root = "C0000";        //ルートのメニュー(出欠管理システム)
    var $menuid;                //メニューID
    var $menuname;
    var $refstaff;                //参照グループ
    var $setstaff;                //設定グループ
    var $authradio = array();
    var $menuidarr = array();
    var $control = array();
    var $path;
    var $ini;  //権限確認から戻るフラグ
    
    function knjz320Model()
    {
        $this->refstaff = "";
        $this->setstaff = "";
        $this->path = "";
        $this->menuid = "";
        $this->menuname = "";
    }
        
    function init()
    {
        $this->cmd      = VARS::request("cmd");

        if (VARS::get("ini_back") != "") {
            $this->ini = VARS::get("ini_back");
        }
        
        $menuid = VARS::request("MENUID");
        if (isset($menuid)){
            $this->menuid   = VARS::request("MENUID");
        }
        
        $menuname = VARS::get("MN");
        if (isset($menuname)){
            $this->menuname   = VARS::get("MN");
        }

        $root = VARS::post("ROOT");
        if (isset($root)){
            $this->root = VARS::post("ROOT");      //ルートのメニューID
        }

        if (VARS::post("refstaff") != "") {
            $this->refstaff = VARS::post("refstaff");
        }
        
        if (VARS::post("setstaff") != "") {
            $this->setstaff = VARS::post("setstaff");
        }
        
        $this->authradio = array();
        for ($i = 0; $i < get_count($this->menuidarr); $i++){
            $this->authradio[$this->menuidarr[$i]] = VARS::post($this->menuidarr[$i]);
        }
        //パスの保存
        $path = VARS::get("PATH");
        if (isset($path)){
            $this->path = VARS::get("PATH");
        }

        $temp = VARS::get("PROGRAMID");
        if (isset($temp)){
            $this->menuid = "";
            $this->refstaff = "";
            $this->setstaff = "";
            $this->ini = "";
        }
        if (VARS::get("PROGRAMID") != "") {
            //起動時のデフォルトでソートを（職員コード）の昇順にする
            $this->s_id = "1";
            $this->sort["1"] = "1";
            $this->sort["2"] = "0";
        } elseif (VARS::get("sort1") != "" || VARS::get("sort2") != "" ) {
            $this->sort["1"] = (VARS::get("sort1") != "" ) ? VARS::get("sort1") : "0";
            $this->sort["2"] = (VARS::get("sort2") != "" ) ? VARS::get("sort2") : "0";
        }

        if (VARS::get("s_id") != ""){
            $this->s_id     = VARS::get("s_id");
        }

        $this->cmd                          = VARS::request("cmd");
        if(VARS::post("SUBSYSTEM_COMBO")){
            $this->subsystem                    = VARS::post("SUBSYSTEM_COMBO");        //プルダウンメニュー
        }

        //プロパティファイル -- menuInfo.properties
        $temp = VARS::get("PROGRAMID");
        if (isset($temp)) {
            $this->getProperties();
        }
        //プロパティファイル -- prgInfo.properties
        $this->getPropertiesAll();
    }

    function getProperties()
    {
        $this->menuProperties = array();
        //初期値
        $this->menuProperties["useRootMenu"] = "C1000";

        $retVal = "";
        
        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/menuInfo.properties")) {
            $filename = CONFDIR ."/menuInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/menuInfo.properties";
        }

        $fp = @fopen($filename, 'r');
        while ($line = fgets($fp,1024)) {
            foreach ($this->menuProperties as $key => $value) {
                $pos = strpos($line, $key);
                if ($pos === false) {
                } else {
                    $retVal = str_replace($key." = ", "", $line);
                    $this->menuProperties[$key] = str_replace("\r\n", "", $retVal);
                }
            }
        }
        fclose($fp);
        $this->root = substr($this->menuProperties["useRootMenu"], 0, 1) . "0000"; //ルートのメニュー(初期値)『頭1桁 + '0000'』

        return;
    }

    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel()
    {
    }
    /**
     * ツリー表示用のモデルを返します
    */
    function &getTreeModel()
    {
    }

    //入力項目チェック
    function validateInput()
    {
         foreach ($this->authradio as $val) {
            if (!is_numeric($val)) {
                return false;
            }
        }        
        return true;
    }
    
    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function &update()
    {
        $result = knjz320Query::getUpdateQuery($this);
        return $result;
    }
}
?>
