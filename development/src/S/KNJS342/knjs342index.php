<?php

require_once('for_php7.php');

require_once('knjs342Model.inc');
require_once('knjs342Query.inc');

class knjs342Controller extends Controller
{
    public $ModelClassName = "knjs342Model";
    public $ProgramID      = "KNJS342";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjs342Form1");
                    break 2;
                case "knjs342":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjs342Model();        //コントロールマスタの呼び出し
                    $this->callView("knjs342Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs342Ctl = new knjs342Controller();
