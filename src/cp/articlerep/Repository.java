package cp.articlerep;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cp.articlerep.ds.Iterator;
import cp.articlerep.ds.LinkedList;
import cp.articlerep.ds.List;
import cp.articlerep.ds.Map;
import cp.articlerep.ds.HashTable;

/**
 * @author Ricardo Dias
 */
public class Repository {

	private Map<String, List<Article>> byAuthor;
	private Map<String, List<Article>> byKeyword;
	private Map<Integer, Article> byArticleId;

	public Repository(int nkeys) {
		this.byAuthor = new HashTable<String, List<Article>>(nkeys*2);
		this.byKeyword = new HashTable<String, List<Article>>(nkeys*2);
		this.byArticleId = new HashTable<Integer, Article>(nkeys*2);
	}
	
	/*
	 * Article insertion in Database: 
	 * Given an article, for each author and keyword refresh byAuthor and byKeyword tables
	 * Insert Article in byArticleId table
	 * */
	public boolean insertArticle(Article a) {

		if (byArticleId.contains(a.getId()))
			return false;

		Iterator<String> authors = a.getAuthors().iterator();
		while (authors.hasNext()) {
			String name = authors.next();

			List<Article> ll = byAuthor.get(name);
			if (ll == null) {
				ll = new LinkedList<Article>();
				byAuthor.put(name, ll);
			}
			ll.add(a);
		}

		Iterator<String> keywords = a.getKeywords().iterator();
		while (keywords.hasNext()) {
			String keyword = keywords.next();

			List<Article> ll = byKeyword.get(keyword);
			if (ll == null) {
				ll = new LinkedList<Article>();
				byKeyword.put(keyword, ll);
			} 
			ll.add(a);
		}

		byArticleId.put(a.getId(), a);

		return true;
	}
	/*Given an id, remove article from byArticleId, byAuthor, byKeyword tables
	 * 
	 * */
	public void removeArticle(int id) {
		Article a = byArticleId.get(id);

		if (a == null)
			return;
		
		byArticleId.remove(id);

		Iterator<String> keywords = a.getKeywords().iterator();
		while (keywords.hasNext()) {
			String keyword = keywords.next();

			List<Article> ll = byKeyword.get(keyword);
			if (ll != null) {
				int pos = 0;
				Iterator<Article> it = ll.iterator();
				while (it.hasNext()) {
					Article toRem = it.next();
					if (toRem == a) {
						break;
					}
					pos++;
				}
				ll.remove(pos);
				it = ll.iterator();
				if (!it.hasNext()) { // checks if the list is empty
					byKeyword.remove(keyword);
				}
			}
		}

		Iterator<String> authors = a.getAuthors().iterator();
		while (authors.hasNext()) {
			String name = authors.next();

			List<Article> ll = byAuthor.get(name);
			if (ll != null) {
				int pos = 0;
				Iterator<Article> it = ll.iterator();
				while (it.hasNext()) {
					Article toRem = it.next();
					if (toRem == a) {
						break;
					}
					pos++;
				}
				ll.remove(pos);
				it = ll.iterator(); 
				if (!it.hasNext()) { // checks if the list is empty
					byAuthor.remove(name);
				}
			}
		}
	}
	/*
	 * Given a Set A of size #nFindList of authors create a set Pi of articles containing i as author
	 * It is a 'read' type operation 
	 * */
	public List<Article> findArticleByAuthor(List<String> authors) {
		List<Article> res = new LinkedList<Article>();

		Iterator<String> it = authors.iterator();
		while (it.hasNext()) {
			String name = it.next();
			List<Article> as = byAuthor.get(name);
			if (as != null) {
				Iterator<Article> ait = as.iterator();
				while (ait.hasNext()) {
					Article a = ait.next();
					res.add(a);
				}
			}
		}

		return res;
	}
	/*
	 * Same as above but with keywords as findList
	 */
	public List<Article> findArticleByKeyword(List<String> keywords) {
		List<Article> res = new LinkedList<Article>();

		Iterator<String> it = keywords.iterator();
		while (it.hasNext()) {
			String keyword = it.next();
			List<Article> as = byKeyword.get(keyword);
			if (as != null) {
				Iterator<Article> ait = as.iterator();
				while (ait.hasNext()) {
					Article a = ait.next();
					res.add(a);
				}
			}
		}

		return res;
	}

	
	/**
	 * This method is supposed to be executed with no concurrent thread
	 * accessing the repository.
	 * 
	 */
	public boolean validate() {
		
		HashSet<Integer> articleIds = new HashSet<Integer>();
		int articleCount = 0;
		
		Iterator<Article> aIt = byArticleId.values();
		while(aIt.hasNext()) { //for each article in Database check its consistency
			Article a = aIt.next();			
			
			articleIds.add(a.getId());
			articleCount++;
			
			// check the authors consistency
			Iterator<String> authIt = a.getAuthors().iterator();
			while(authIt.hasNext()) {
				String name = authIt.next();
				if (!searchAuthorArticle(a, name)) { //check if author has article 'a'
					return false;
				}
			}
			
			// check the keywords consistency
			Iterator<String> keyIt = a.getKeywords().iterator();
			while(keyIt.hasNext()) {
				String keyword = keyIt.next();
				if (!searchKeywordArticle(a, keyword)) { //check if keyword as reference to article 'a'
					return false;
				}
			}
		}
		
		/*Invariantes extraordinarias inventadas por nós*/
		Iterator<List<Article>> byAuthList= byAuthor.values();
		
		while(byAuthList.hasNext()){ //verifies if does'nt exists 'phantom' articles i.e if a author
							//contains an article that does not exists in byArticleId table then it is a inconcistency
			List<Article> l = byAuthList.next();
			Iterator<Article> ait = l.iterator();
			while(ait.hasNext()){
				if(!byArticleId.contains(ait.next().getId()))
					return false;
			}
		}
		
		Iterator<List<Article>> byKWList= byKeyword.values();
		
		while(byKWList.hasNext()){ //Same as above but with keywords
			List<Article> l = byKWList.next();
			Iterator<Article> ait = l.iterator();
			while(ait.hasNext()){
				if(!byArticleId.contains(ait.next().getId()))
					return false;
			}
		}
		
		return articleCount == articleIds.size();
	}
	/*
	 * Verifies if author has article a
	 */
	private boolean searchAuthorArticle(Article a, String author) {
		List<Article> ll = byAuthor.get(author);
		if (ll != null) {
			Iterator<Article> it = ll.iterator();
			while (it.hasNext()) {
				if (it.next() == a) {
					return true;
				}
			}
		}
		return false;
	}
	/*
	 * Verifies if keyword has a reference to article a
	 */
	private boolean searchKeywordArticle(Article a, String keyword) {
		List<Article> ll = byKeyword.get(keyword);
		if (ll != null) {
			Iterator<Article> it = ll.iterator();
			while (it.hasNext()) {
				if (it.next() == a) {
					return true;
				}
			}
		}
		return false;
	}

}
