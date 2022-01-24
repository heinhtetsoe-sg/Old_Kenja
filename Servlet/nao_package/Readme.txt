// kanji=漢字

◆ビルド方法
ant dist

◆バージョン確認方法
jar ファイル中のマニフェストを参照せよ。
// TODO: build.number ファイルは CVS で管理してみてはどうか?

◆変更履歴
・TAG-NAO_PACKAGE-VERSION-1_6-FOR-KENJA
	Modified Files:
		Database.java 
	Log Message:
	★ Connectionなフィールドを protected から public に変えた
	  -- これで「executeUpdate をコールする前に1度は query をコールしないといけないバグ」に対応可能。
	  -- 他にも融通が利きそうなので。
	
	★ rollbackを deprecated にした。
	  -- 標準出力を使っているので非推奨とする
	  -- 他にも deprecated にすべきメソッドがあるが、影響範囲を考慮し、今回は rollback だけ。


・TAG-NAO_PACKAGE-VERSION-1_5-FOR-KENJA
	)))未調査(((
