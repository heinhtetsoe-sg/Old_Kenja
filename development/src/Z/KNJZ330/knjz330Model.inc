<?php

require_once('for_php7.php');

//メニュー画面
class knjz330Model extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $root = "C0000";        //ルートのメニュー(出欠管理システム)
    var $menuid;                //メニューID
    var $menuname;
    var $refgroup;                //参照グループ
    var $setgroup;                //設定グループ
    var $authradio = array();
    var $menuidarr = array();
    var $path;
    var $ini;
    
    function knjz330Model()
    {
        $this->refgroup = "";
        $this->setgroup = "";
        $this->path = "";
        $this->menuid = "";
        $this->menuname = "";
    }
       
    function init()
    {
        $this->cmd      = VARS::request("cmd");
        if (VARS::get("ini") != "") {
            $this->ini = VARS::get("ini");
        }
        $menuid = VARS::request("MENUID");
        if (isset($menuid)){
            $this->menuid   = VARS::request("MENUID");
        }
        
        $root = VARS::post("ROOT");
        if (isset($root)){
            $this->root     = VARS::post("ROOT");      //ルートのメニューID
        }
 
        $menuname = VARS::get("MN");
        if (isset($menuname)){
            $this->menuname   = VARS::get("MN");
        }

        if (VARS::post("refgroup") != "") {
            $this->refgroup = VARS::post("refgroup");
        }
        if (VARS::post("setgroup") != "") {
            $this->setgroup = VARS::post("setgroup");
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
            $this->ini = "";
            $this->refgroup = "";
            $this->setgroup = "";
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
        $menuProperties = array();
        //初期値
        $menuProperties["useRootMenu"] = "C1000";

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
            foreach ($menuProperties as $key => $value) {
                $pos = strpos($line, $key);
                if ($pos === false) {
                } else {
                    $retVal = str_replace($key." = ", "", $line);
                    $menuProperties[$key] = str_replace("\r\n", "", $retVal);
                }
            }
        }
        fclose($fp);
        $this->root = substr($menuProperties["useRootMenu"], 0, 1) . "0000"; //ルートのメニュー(初期値)『頭1桁 + '0000'』

        return;
    }
    
    function &getMainModel()
    {
    }
    
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
        $result = knjz330Query::getUpdateQuery($this);
        return $result;
    }
}
?>
