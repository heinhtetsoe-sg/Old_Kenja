<?php

require_once('for_php7.php');

require_once('knjc201_2Model.inc');
require_once('knjc201_2Query.inc');

class knjc201_2Controller extends Controller
{
    public $ModelClassName = "knjc201_2Model";
    public $ProgramID      = "KNJC201";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "update":  //更新
                    $sessionInstance->setAccessLogDetail("U", "KNJC201_2");
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("schno");
                    break 1;
                case "delete":  //削除
                    $sessionInstance->setAccessLogDetail("D", "KNJC201_2");
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("schno");
                    break 1;
                case "":
                case "schno":
                    $sessionInstance->setAccessLogDetail("S", "KNJC201_2");
                    $sessionInstance->knjc201_2Model();     //コントロールマスタの呼び出し
                    $this->callView("knjc201_2Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc201_2Ctl = new knjc201_2Controller();
//var_dump($_REQUEST);
