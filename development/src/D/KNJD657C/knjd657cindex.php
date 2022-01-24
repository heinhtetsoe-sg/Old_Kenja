<?php

require_once('for_php7.php');

require_once('knjd657cModel.inc');
require_once('knjd657cQuery.inc');

class knjd657cController extends Controller
{
    public $ModelClassName = "knjd657cModel";
    public $ProgramID      = "KNJD657C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":       //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd657cForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjd657cForm1");
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
$knjd657cCtl = new knjd657cController();
