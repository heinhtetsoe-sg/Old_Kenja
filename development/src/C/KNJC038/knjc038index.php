<?php

require_once('for_php7.php');

require_once('knjc038Model.inc');
require_once('knjc038Query.inc');

class knjc038Controller extends Controller
{
    public $ModelClassName = "knjc038Model";
    public $ProgramID      = "KNJC038";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":            //データ取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv_error":       //エラー出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjc038Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "csv_header":       //ヘッダ出力
                    if (!$sessionInstance->getHeaderModel()) {
                        $this->callView("knjc038Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjc038Form1");
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
$knjc038Ctl = new knjc038Controller();
