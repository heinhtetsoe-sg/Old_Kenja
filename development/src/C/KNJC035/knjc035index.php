<?php

require_once('for_php7.php');

require_once('knjc035Model.inc');
require_once('knjc035Query.inc');

class knjc035Controller extends Controller
{
    public $ModelClassName = "knjc035Model";
    public $ProgramID      = "KNJC035";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":			//データ取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv_error":   	//エラー出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjc035Form1");
                    }
                    break 2;
                case "csv_header":   	//ヘッダ出力
                    if (!$sessionInstance->getHeaderModel()) {
                        $this->callView("knjc035Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjc035Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc035Ctl = new knjc035Controller();
